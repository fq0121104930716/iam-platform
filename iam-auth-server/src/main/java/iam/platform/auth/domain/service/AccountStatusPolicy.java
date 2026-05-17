package iam.platform.auth.domain.service;

import iam.platform.auth.domain.model.entity.Person;
import iam.platform.auth.domain.model.entity.TenantAccount;

/**
 * Domain service for validating person and tenant account status before and during authentication.
 */
public interface AccountStatusPolicy {

    /**
     * Validate that the person account is in a valid state for authentication.
     * 
     * @throws IllegalStateException if person is disabled or locked
     */
    void validatePersonStatus(Person person);

    /**
     * Validate that the tenant account is in a valid state.
     * 
     * @throws IllegalStateException if tenant account is not active
     */
    void validateTenantAccountStatus(TenantAccount account);
}
