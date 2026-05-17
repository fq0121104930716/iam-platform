package iam.platform.admin.domain.service;

/**
 * Domain service for validating preconditions when creating a TenantAccount.
 * This logic requires cross-aggregate validation (Tenant state + uniqueness).
 */
public interface TenantAccountCreationPolicy {

    /**
     * Validate all preconditions for creating a tenant account.
     * Checks: tenant is ACTIVE, accountCode is unique within tenant,
     * employeeNo is unique within tenant (if provided).
     *
     * @param tenantId    the tenant to create account in
     * @param accountCode the desired account code
     * @param employeeNo  the employee number (nullable)
     * @throws iam.platform.common.model.exception.InvalidStateException if tenant is
     *                                                               not ACTIVE
     * @throws iam.platform.common.model.exception.ConflictException     if codes are
     *                                                               not unique
     */
    void validateCreationPreconditions(Long tenantId, String accountCode, String employeeNo);
}
