package iam.platform.common.model.enums;

/**
 * Audit event category enumeration.
 */
public enum EventCategory {
    AUTHENTICATION, // Authentication events (login, logout, token refresh)
    AUTHORIZATION, // Authorization events (role assignment, permission changes)
    ACCOUNT, // Account events (create, update, lock, password change)
    ADMINISTRATION, // Administration events (tenant/app/organization management)
    SESSION // Session events (tenant switch, session expiry)
}
