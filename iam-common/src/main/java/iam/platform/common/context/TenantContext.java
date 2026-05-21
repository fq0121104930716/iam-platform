package iam.platform.common.context;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Thread-local storage for current tenant context. Stores the authenticated User's ID, username,
 * current tenant ID, and current tenant account ID for the request lifecycle.
 *
 * <p>
 * This class is shared across all downstream services to eliminate code duplication. The gateway is
 * responsible for extracting tenant information from JWT claims and setting standard HTTP headers,
 * which downstream services then read via {@link #populateFromHeaders(HttpServletRequest)}.
 */
public final class TenantContext {

    // Standard HTTP header names for tenant context propagation
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_NAME = "X-User-Name";
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";
    public static final String HEADER_TENANT_ACCOUNT_ID = "X-Tenant-Account-Id";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String HEADER_USER_PERMISSIONS = "X-User-Permissions";

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USER_NAME = new ThreadLocal<>();
    private static final ThreadLocal<Long> CURRENT_TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> CURRENT_TENANT_ACCOUNT_ID = new ThreadLocal<>();

    private TenantContext() {}

    public static Long getCurrentUserId() {
        return CURRENT_USER_ID.get();
    }

    public static void setCurrentUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static String getCurrentUserName() {
        return CURRENT_USER_NAME.get();
    }

    public static void setCurrentUserName(String userName) {
        CURRENT_USER_NAME.set(userName);
    }

    public static Long getCurrentTenantId() {
        return CURRENT_TENANT_ID.get();
    }

    public static void setCurrentTenantId(Long tenantId) {
        CURRENT_TENANT_ID.set(tenantId);
    }

    public static Long getCurrentTenantAccountId() {
        return CURRENT_TENANT_ACCOUNT_ID.get();
    }

    public static void setCurrentTenantAccountId(Long tenantAccountId) {
        CURRENT_TENANT_ACCOUNT_ID.set(tenantAccountId);
    }

    /**
     * Populate tenant context from standard HTTP headers set by the gateway.
     *
     * <p>
     * This method is fault-tolerant: if any header value cannot be parsed, it will be skipped
     * rather than failing the entire request.
     *
     * @param request the HTTP servlet request containing gateway headers
     */
    public static void populateFromHeaders(HttpServletRequest request) {
        String userId = request.getHeader(HEADER_USER_ID);
        if (userId != null && !userId.isBlank()) {
            try {
                setCurrentUserId(Long.parseLong(userId.trim()));
            } catch (NumberFormatException e) {
                // Skip invalid header value - don't fail the request
            }
        }

        String userName = request.getHeader(HEADER_USER_NAME);
        if (userName != null && !userName.isBlank()) {
            setCurrentUserName(userName.trim());
        }

        String tenantId = request.getHeader(HEADER_TENANT_ID);
        if (tenantId != null && !tenantId.isBlank()) {
            try {
                setCurrentTenantId(Long.parseLong(tenantId.trim()));
            } catch (NumberFormatException e) {
                // Skip invalid header value - don't fail the request
            }
        }

        String tenantAccountId = request.getHeader(HEADER_TENANT_ACCOUNT_ID);
        if (tenantAccountId != null && !tenantAccountId.isBlank()) {
            try {
                setCurrentTenantAccountId(Long.parseLong(tenantAccountId.trim()));
            } catch (NumberFormatException e) {
                // Skip invalid header value - don't fail the request
            }
        }
    }

    public static void clear() {
        CURRENT_USER_ID.remove();
        CURRENT_USER_NAME.remove();
        CURRENT_TENANT_ID.remove();
        CURRENT_TENANT_ACCOUNT_ID.remove();
    }
}
