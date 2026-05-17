package iam.platform.auth.infrastructure.security;

/**
 * Thread-local storage for current tenant context. Stores the authenticated person's ID, current
 * tenant ID, and current tenant account ID for the request lifecycle.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_PERSON_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> CURRENT_TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> CURRENT_TENANT_ACCOUNT_ID = new ThreadLocal<>();

    private TenantContext() {}

    public static Long getCurrentPersonId() {
        return CURRENT_PERSON_ID.get();
    }

    public static void setCurrentPersonId(Long personId) {
        CURRENT_PERSON_ID.set(personId);
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
        CURRENT_PERSON_ID.remove();
        CURRENT_TENANT_ID.remove();
        CURRENT_TENANT_ACCOUNT_ID.remove();
    }
}
