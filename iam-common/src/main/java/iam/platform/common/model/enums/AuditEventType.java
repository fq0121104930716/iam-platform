package iam.platform.common.model.enums;

/**
 * Audit event type enumeration with category mapping.
 */
public enum AuditEventType {
    // AUTHENTICATION
    LOGIN_SUCCESS(EventCategory.AUTHENTICATION), LOGIN_FAILURE(
            EventCategory.AUTHENTICATION), LOGOUT(EventCategory.AUTHENTICATION), TOKEN_REFRESH(
                    EventCategory.AUTHENTICATION), SMS_CODE_SENT(
                            EventCategory.AUTHENTICATION), EMAIL_CODE_SENT(
                                    EventCategory.AUTHENTICATION),

    // AUTHORIZATION
    ROLE_ASSIGN(EventCategory.AUTHORIZATION), ROLE_REVOKE(
            EventCategory.AUTHORIZATION), PERMISSION_CHANGE(EventCategory.AUTHORIZATION),

    // ACCOUNT
    PERSON_CREATED(EventCategory.ACCOUNT), PERSON_UPDATED(EventCategory.ACCOUNT), PERSON_DELETED(
            EventCategory.ACCOUNT), TENANT_ACCOUNT_CREATED(
                    EventCategory.ACCOUNT), TENANT_ACCOUNT_UPDATED(
                            EventCategory.ACCOUNT), PASSWORD_CHANGED(
                                    EventCategory.ACCOUNT), ACCOUNT_LOCKED(
                                            EventCategory.ACCOUNT), ACCOUNT_UNLOCKED(
                                                    EventCategory.ACCOUNT),

    // ADMINISTRATION
    TENANT_CREATED(EventCategory.ADMINISTRATION), TENANT_UPDATED(
            EventCategory.ADMINISTRATION), TENANT_ACTIVATED(
                    EventCategory.ADMINISTRATION), TENANT_SUSPENDED(
                            EventCategory.ADMINISTRATION), TENANT_DELETED(
                                    EventCategory.ADMINISTRATION), ORGANIZATION_CREATED(
                                            EventCategory.ADMINISTRATION), ORGANIZATION_UPDATED(
                                                    EventCategory.ADMINISTRATION), ORGANIZATION_DELETED(
                                                            EventCategory.ADMINISTRATION), APPLICATION_CREATED(
                                                                    EventCategory.ADMINISTRATION), APPLICATION_UPDATED(
                                                                            EventCategory.ADMINISTRATION), APPLICATION_DELETED(
                                                                                    EventCategory.ADMINISTRATION), APPLICATION_ACTIVATED(
                                                                                            EventCategory.ADMINISTRATION), APPLICATION_BLOCKED(
                                                                                                    EventCategory.ADMINISTRATION), ROLE_CREATED(
                                                                                                            EventCategory.ADMINISTRATION), ROLE_DELETED(
                                                                                                                    EventCategory.ADMINISTRATION), PERMISSION_CREATED(
                                                                                                                            EventCategory.ADMINISTRATION), PERMISSION_DELETED(
                                                                                                                                    EventCategory.ADMINISTRATION),

    // SESSION
    TENANT_SELECTED(EventCategory.SESSION), SESSION_EXPIRED(EventCategory.SESSION);

    private final EventCategory category;

    AuditEventType(EventCategory category) {
        this.category = category;
    }

    public EventCategory getCategory() {
        return category;
    }
}
