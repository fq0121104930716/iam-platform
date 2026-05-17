package iam.platform.auth.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import iam.platform.auth.domain.model.entity.Person;
import iam.platform.auth.domain.model.entity.Tenant;
import iam.platform.auth.domain.model.entity.TenantAccount;
import iam.platform.auth.domain.repository.TenantAccountRepository;
import iam.platform.auth.domain.repository.TenantRepository;
import iam.platform.auth.domain.model.valueobject.TenantResolutionResult;
import iam.platform.auth.domain.service.TenantResolutionPolicy;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantResolutionPolicyImpl implements TenantResolutionPolicy {

    private final TenantAccountRepository tenantAccountRepository;
    private final TenantRepository tenantRepository;

    @Override
    public TenantResolutionResult resolve(Person person, String requestedTenantCode) {
        List<TenantAccount> activeAccounts = tenantAccountRepository.findByPersonId(person.getId())
                .stream().filter(TenantAccount::isActive).collect(Collectors.toList());

        if (activeAccounts.isEmpty()) {
            return TenantResolutionResult.noAccounts();
        }

        // If a specific tenant code is requested, try to select it
        if (requestedTenantCode != null && !requestedTenantCode.isBlank()) {
            Tenant tenant = tenantRepository.findByTenantCode(requestedTenantCode).orElse(null);
            if (tenant != null && tenant.isActive()) {
                TenantAccount requestedAccount = activeAccounts.stream()
                        .filter(acc -> acc.getTenantId().equals(tenant.getId())).findFirst()
                        .orElse(null);
                if (requestedAccount != null) {
                    return TenantResolutionResult.autoSelected(requestedAccount, activeAccounts);
                }
            }
        }

        // Auto-select if exactly one active account
        if (activeAccounts.size() == 1) {
            return TenantResolutionResult.autoSelected(activeAccounts.get(0), activeAccounts);
        }

        // Multiple accounts - require UI selection
        return TenantResolutionResult.selectionRequired(activeAccounts);
    }
}
