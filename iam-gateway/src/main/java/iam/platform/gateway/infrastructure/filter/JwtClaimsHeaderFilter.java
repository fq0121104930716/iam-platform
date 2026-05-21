package iam.platform.gateway.infrastructure.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;

/**
 * WebFlux filter that extracts user and tenant information from the authenticated JWT token
 * and adds them as standard HTTP headers for downstream services.
 *
 * <p>This filter runs after Spring Security's authentication filter and before gateway routing.
 * It reads the JWT claims from the SecurityContext and mutates the outgoing request with headers:
 * <ul>
 *   <li>X-User-Id: User ID from 'user_id' claim</li>
 *   <li>X-User-Name: User name from 'nickname' or 'sub' claim</li>
 *   <li>X-Tenant-Id: Tenant ID from 'tenant_id' claim (may be null)</li>
 *   <li>X-Tenant-Account-Id: Tenant account ID from 'tenant_account_id' claim</li>
 *   <li>X-User-Roles: JSON array of roles from 'roles' claim</li>
 *   <li>X-User-Permissions: JSON array of permissions from 'permissions' claim</li>
 * </ul>
 *
 * <p>Public paths (/login/**, /auth/**, etc.) are skipped to avoid adding headers
 * to internal OAuth2 flows.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtClaimsHeaderFilter implements WebFilter, Ordered {

    // Standard header names (matching TenantContext constants)
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_NAME = "X-User-Name";
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";
    public static final String HEADER_TENANT_ACCOUNT_ID = "X-Tenant-Account-Id";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String HEADER_USER_PERMISSIONS = "X-User-Permissions";

    private static final String AUTHENTICATION_ATTRIBUTE = "org.springframework.security.web.server.context.authentication";

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Skip public paths - no JWT validation happened, so no headers to add
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Get authentication from exchange attributes (set by Spring Security)
        Authentication auth = exchange.getAttribute(AUTHENTICATION_ATTRIBUTE);
        if (auth == null || auth.getPrincipal() == null) {
            // Not authenticated - skip header addition
            return chain.filter(exchange);
        }

        // Extract JWT from authentication
        if (!(auth.getPrincipal() instanceof Jwt jwt)) {
            log.debug("Principal is not a JWT, skipping header extraction: {}",
                    auth.getPrincipal().getClass().getSimpleName());
            return chain.filter(exchange);
        }

        // Build mutated request with additional headers
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .headers(headers -> {
                    // Extract and add user ID
                    Long userId = getClaimAsLong(jwt, "user_id");
                    if (userId != null) {
                        headers.add(HEADER_USER_ID, userId.toString());
                    }

                    // Extract and add user name
                    String userName = jwt.getClaimAsString("nickname");
                    if (userName == null) {
                        userName = jwt.getSubject();
                    }
                    if (userName != null) {
                        headers.add(HEADER_USER_NAME, userName);
                    }

                    // Extract and add tenant ID (may be null if user hasn't selected a tenant)
                    Long tenantId = getClaimAsLong(jwt, "tenant_id");
                    if (tenantId != null) {
                        headers.add(HEADER_TENANT_ID, tenantId.toString());
                    }

                    // Extract and add tenant account ID
                    Long tenantAccountId = getClaimAsLong(jwt, "tenant_account_id");
                    if (tenantAccountId != null) {
                        headers.add(HEADER_TENANT_ACCOUNT_ID, tenantAccountId.toString());
                    }

                    // Extract and serialize roles
                    serializeAndAddHeader(jwt, "roles", headers, HEADER_USER_ROLES);

                    // Extract and serialize permissions
                    serializeAndAddHeader(jwt, "permissions", headers, HEADER_USER_PERMISSIONS);
                })
                .build();

        log.debug("JWT claims extracted and headers added for path: {}", path);

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        // Run after Spring Security filters (typically order -100) but before routing
        // Security filters are around order -100, so we use -50 to run after them
        return -50;
    }

    /**
     * Check if a path is public (should skip header extraction).
     */
    private boolean isPublicPath(String path) {
        return path.startsWith("/login/")
                || path.startsWith("/oauth2/")
                || path.startsWith("/auth/")
                || path.startsWith("/static/")
                || path.startsWith("/actuator/")
                || path.startsWith("/.well-known/")
                || path.equals("/favicon.ico")
                || path.equals("/error");
    }

    /**
     * Extract a claim as Long, handling various numeric types.
     */
    private Long getClaimAsLong(Jwt jwt, String claimName) {
        Object value = jwt.getClaims().get(claimName);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            log.warn("Invalid {} claim format: {}", claimName, value);
            return null;
        }
    }

    /**
     * Extract a collection claim and serialize it as JSON array string.
     */
    private void serializeAndAddHeader(Jwt jwt, String claimName,
            org.springframework.http.HttpHeaders headers, String headerName) {
        Object claim = jwt.getClaims().get(claimName);
        if (claim == null) {
            return;
        }

        Collection<?> collection;
        if (claim instanceof Collection) {
            collection = (Collection<?>) claim;
        } else {
            // Single value - wrap in list
            collection = Collections.singletonList(claim);
        }

        if (!collection.isEmpty()) {
            try {
                String json = objectMapper.writeValueAsString(collection);
                headers.add(headerName, json);
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize {} claim: {}", claimName, e.getMessage());
            }
        }
    }
}
