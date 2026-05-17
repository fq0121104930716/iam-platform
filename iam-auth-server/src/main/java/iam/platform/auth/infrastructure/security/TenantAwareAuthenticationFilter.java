package iam.platform.auth.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

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

    private static final String SESSION_TENANT_ID = "SESSION_TENANT_ID";
    private static final String SESSION_TENANT_ACCOUNT_ID = "SESSION_TENANT_ACCOUNT_ID";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            restoreTenantContextFromSession(request);
        } catch (Exception e) {
            log.debug("Failed to restore tenant context from session: {}", e.getMessage());
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
            Object tenantId = session.getAttribute(SESSION_TENANT_ID);
            Object tenantAccountId = session.getAttribute(SESSION_TENANT_ACCOUNT_ID);

            if (tenantId != null && tenantAccountId != null) {
                TenantContext.setCurrentTenantId((Long) tenantId);
                TenantContext.setCurrentTenantAccountId((Long) tenantAccountId);

                // Person ID is already stored in TenantContext during login pipeline
                Long personId = TenantContext.getCurrentPersonId();
                if (personId != null) {
                    TenantContext.setCurrentPersonId(personId);
                }
            }
        }
    }
}
