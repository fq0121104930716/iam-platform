package iam.platform.admin.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * HTTP request interceptor that extracts tenant information from the request and populates the
 * TenantContext for the duration of the request.
 *
 * Tenant identification strategies (in priority order): 1. X-Tenant-Id header 2. tenant_id query
 * parameter
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final String HEADER_TENANT_ID = "X-Tenant-Id";
    private static final String PARAM_TENANT_ID = "tenant_id";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response, @NonNull Object handler) {
        String tenantIdStr = request.getHeader(HEADER_TENANT_ID);

        if (tenantIdStr == null || tenantIdStr.isBlank()) {
            tenantIdStr = request.getParameter(PARAM_TENANT_ID);
        }

        if (tenantIdStr != null && !tenantIdStr.isBlank()) {
            try {
                Long tenantId = Long.parseLong(tenantIdStr.trim());
                TenantContext.setCurrentTenantId(tenantId);
            } catch (NumberFormatException ignored) {
                // Invalid tenant ID format, skip setting context
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response, @NonNull Object handler,
            @Nullable Exception ex) {
        TenantContext.clear();
    }
}
