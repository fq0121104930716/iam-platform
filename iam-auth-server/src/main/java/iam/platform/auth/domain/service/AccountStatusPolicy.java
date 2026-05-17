package iam.platform.auth.domain.service;

import iam.platform.auth.domain.model.entity.User;
import iam.platform.auth.domain.model.entity.TenantAccount;

/**
 * Domain service for validating User and tenant account status before and during authentication.
 */
public interface AccountStatusPolicy {

    /**
     * Validate that the User account is in a valid state for authentication.
     * 
     * @throws IllegalStateException if User is disabled or locked
     */
    void validateUserStatus(User user);

    /**
     * Validate that the tenant account is in a valid state.
     * 
     * @throws IllegalStateException if tenant account is not active
     */
    void validateTenantAccountStatus(TenantAccount account);
}
