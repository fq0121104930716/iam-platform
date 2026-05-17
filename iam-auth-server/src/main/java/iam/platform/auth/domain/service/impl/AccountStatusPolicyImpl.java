package iam.platform.auth.domain.service.impl;

import org.springframework.stereotype.Service;
import iam.platform.auth.domain.model.entity.Person;
import iam.platform.auth.domain.model.entity.TenantAccount;
import iam.platform.auth.domain.service.AccountStatusPolicy;

@Service
public class AccountStatusPolicyImpl implements AccountStatusPolicy {

    @Override
    public void validatePersonStatus(Person person) {
        if (!person.isEnabled()) {
            throw new IllegalStateException("Account is disabled");
        }
        if (person.isAccountLocked()) {
            throw new IllegalStateException("Account is locked");
        }
    }

    @Override
    public void validateTenantAccountStatus(TenantAccount account) {
        if (!account.isActive()) {
            throw new IllegalStateException("Tenant account is not active");
        }
    }
}
