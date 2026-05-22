package iam.platform.auth.interfaces.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import iam.platform.common.context.TenantContext;

import java.io.IOException;

/**
 * Tenant context filter that extracts tenant information from gateway headers.
 *
 * <p>
 * All browser requests route through the gateway, which extracts tenant context from JWT tokens and
 * sets standard HTTP headers (X-User-Id, X-Tenant-Id, etc.). This filter populates the
 * TenantContext ThreadLocal from these headers for downstream business logic.
 *
 * <p>
 * The filter runs early in the chain, sets up the context before business logic executes, and
 * guarantees cleanup in a finally block to prevent ThreadLocal leaks.
 */
@Slf4j
@Component
public class TenantAwareAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        // Extract tenant context from gateway headers (all requests route through gateway)
        String userId = request.getHeader(TenantContext.HEADER_USER_ID);
        if (userId != null && !userId.isEmpty()) {
            try {
                TenantContext.populateFromHeaders(request);
                log.debug("Tenant context populated from gateway headers: userId={}", userId);
            } catch (Exception e) {
                log.debug("Failed to populate tenant context from gateway headers: {}",
                        e.getMessage());
                // Clean up partially populated context to avoid inconsistent state
                TenantContext.clear();
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clean up ThreadLocal to prevent memory leaks
            TenantContext.clear();
        }
    }
}
