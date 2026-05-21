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
 * Simplified tenant context filter.
 *
 * After the refactoring, the initial tenant resolution, permission loading, and token construction
 * are handled by the PostAuthenticationPipeline during authentication. This filter only restores
 * tenant context from session for subsequent requests after login.
 */
@Slf4j
@Component
public class TenantAwareAuthenticationFilter extends OncePerRequestFilter {

    private static final String SESSION_USER_ID = "SESSION_USER_ID";
    private static final String SESSION_TENANT_ID = "SESSION_TENANT_ID";
    private static final String SESSION_TENANT_ACCOUNT_ID = "SESSION_TENANT_ACCOUNT_ID";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        // Priority 1: Try to populate from gateway headers (API calls via gateway)
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

        // Priority 2: Fall back to session restoration (browser login scenario)
        if (TenantContext.getCurrentTenantId() == null) {
            try {
                restoreTenantContextFromSession(request);
            } catch (Exception e) {
                log.debug("Failed to restore tenant context from session: {}", e.getMessage());
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Clean up ThreadLocal to prevent memory leaks
            TenantContext.clear();
        }
    }

    /**
     * Restore tenant context from session attributes for already-authenticated requests.
     */
    private void restoreTenantContextFromSession(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) {
            Object userId = session.getAttribute(SESSION_USER_ID);
            Object tenantId = session.getAttribute(SESSION_TENANT_ID);
            Object tenantAccountId = session.getAttribute(SESSION_TENANT_ACCOUNT_ID);

            if (tenantId != null && tenantAccountId != null) {
                if (userId != null) {
                    TenantContext.setCurrentUserId((Long) userId);
                }
                TenantContext.setCurrentTenantId((Long) tenantId);
                TenantContext.setCurrentTenantAccountId((Long) tenantAccountId);
            }
        }
    }
}
