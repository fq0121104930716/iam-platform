package iam.platform.admin.infrastructure.security;

/**
 * Thread-local storage for current tenant context. Stores the authenticated User's ID, current
 * tenant ID, and current tenant account ID for the request lifecycle.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> CURRENT_TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> CURRENT_TENANT_ACCOUNT_ID = new ThreadLocal<>();

    private TenantContext() {}

    public static Long getCurrentUserId() {
        return CURRENT_USER_ID.get();
    }

    public static void setCurrentUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
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

    public static void clear() {
        CURRENT_USER_ID.remove();
        CURRENT_TENANT_ID.remove();
        CURRENT_TENANT_ACCOUNT_ID.remove();
    }
}
