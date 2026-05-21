package iam.platform.admin.infrastructure.security;

import iam.platform.common.context.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * JWT User Context Filter - Extracts user context from Gateway headers. Gateway passes user
 * information via X-User-* headers after JWT validation.
 *
 * <p>This filter:
 * <ul>
 *   <li>Reads tenant context from gateway headers via {@link TenantContext#populateFromHeaders}</li>
 *   <li>Parses roles and permissions for Spring Security Authentication</li>
 *   <li>Guarantees ThreadLocal cleanup in finally block</li>
 * </ul>
 */
@Slf4j
@Component
public class JwtUserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader(TenantContext.HEADER_USER_ID);

        if (userId != null && !userId.isEmpty()) {
            try {
                // Populate tenant context from gateway headers
                TenantContext.populateFromHeaders(request);

                // Extract roles and permissions for Spring Security
                String roles = request.getHeader(TenantContext.HEADER_USER_ROLES);
                String permissions = request.getHeader(TenantContext.HEADER_USER_PERMISSIONS);

                // Create Authentication object
                Collection<GrantedAuthority> authorities = parseAuthorities(roles, permissions);
                Authentication auth =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);

                log.debug("User context established from Gateway headers: userId={}, tenantId={}",
                        userId, TenantContext.getCurrentTenantId());
            } catch (Exception e) {
                log.error("Failed to extract user context from Gateway headers: {}",
                        e.getMessage());
                // Continue without authentication - let Spring Security handle it
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Always clean up to prevent ThreadLocal leaks
            TenantContext.clear();
        }
    }

    private Collection<GrantedAuthority> parseAuthorities(String roles, String permissions) {
        Collection<GrantedAuthority> authorities = new java.util.ArrayList<>();

        if (roles != null && !roles.isEmpty()) {
            // Parse JSON array format: ["admin","user"]
            String[] roleArray = roles.replaceAll("[\\[\\]\"]", "").split(",");
            Arrays.stream(roleArray).map(String::trim).filter(r -> !r.isEmpty())
                    .forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r)));
        }

        if (permissions != null && !permissions.isEmpty()) {
            // Parse JSON array format: ["user:create","tenant:read"]
            String[] permArray = permissions.replaceAll("[\\[\\]\"]", "").split(",");
            Arrays.stream(permArray).map(String::trim).filter(p -> !p.isEmpty())
                    .forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
        }

        return authorities.isEmpty()
                ? Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                : authorities;
    }
}
