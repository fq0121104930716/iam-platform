package iam.platform.admin.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import iam.platform.admin.domain.model.entity.Tenant;
import iam.platform.common.model.exception.ConflictException;
import iam.platform.common.model.exception.InvalidStateException;
import iam.platform.common.model.exception.TenantNotFoundException;
import iam.platform.admin.domain.repository.TenantAccountRepository;
import iam.platform.admin.domain.repository.TenantRepository;
import iam.platform.admin.domain.service.TenantAccountCreationPolicy;

@Service
@RequiredArgsConstructor
public class TenantAccountCreationPolicyImpl implements TenantAccountCreationPolicy {

    private final TenantRepository tenantRepository;
    private final TenantAccountRepository tenantAccountRepository;

    @Override
    public void validateCreationPreconditions(Long tenantId, String accountCode,
            String employeeNo) {
        // Check tenant exists and is ACTIVE
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(
                        "Tenant not found: " + tenantId));
        if (!tenant.isActive()) {
            throw new InvalidStateException(
                    "Cannot create account in non-active tenant: " + tenant.getTenantCode());
        }

        // Check accountCode uniqueness within tenant
        if (tenantAccountRepository.existsByTenantIdAndAccountCode(tenantId, accountCode)) {
            throw new ConflictException(
                    "Account code already exists in this tenant: " + accountCode);
        }

        // Check employeeNo uniqueness within tenant (if provided)
        if (employeeNo != null && !employeeNo.isBlank()
                && tenantAccountRepository.existsByTenantIdAndEmployeeNo(tenantId, employeeNo)) {
            throw new ConflictException(
                    "Employee number already exists in this tenant: " + employeeNo);
        }
    }
}
