package iam.platform.common.context;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Generic servlet filter that extracts tenant context from standard HTTP headers set by the gateway
 * and populates the {@link TenantContext}.
 *
 * <p>This filter should be registered in all downstream services (admin-server, auth-server,
 * bff-server) to automatically extract tenant information from gateway headers.
 *
 * <p>The filter runs early in the chain, sets up the context before business logic executes,
 * and guarantees cleanup in a finally block to prevent ThreadLocal leaks.
 */
@Slf4j
public class GatewayHeaderContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String userId = request.getHeader(TenantContext.HEADER_USER_ID);

        // Only populate if gateway headers are present (allows anonymous requests to pass through)
        if (userId != null && !userId.isEmpty()) {
            try {
                TenantContext.populateFromHeaders(request);
                log.debug("Tenant context populated from gateway headers: userId={}, tenantId={}",
                        userId, TenantContext.getCurrentTenantId());
            } catch (Exception e) {
                log.error("Failed to populate tenant context from gateway headers: {}",
                        e.getMessage());
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Always clean up to prevent ThreadLocal leaks
            TenantContext.clear();
        }
    }
}
