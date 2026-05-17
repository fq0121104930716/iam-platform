package iam.platform.admin.domain.repository;

import iam.platform.admin.domain.model.entity.TenantAccountOrganizationMapping;

import java.util.List;
import java.util.Optional;

public interface TenantAccountOrganizationMappingRepository {
    TenantAccountOrganizationMapping save(TenantAccountOrganizationMapping mapping);

    Optional<TenantAccountOrganizationMapping> findById(Long id);

    List<TenantAccountOrganizationMapping> findByTenantAccountId(Long tenantAccountId);

    List<TenantAccountOrganizationMapping> findByOrganizationId(Long organizationId);

    Optional<TenantAccountOrganizationMapping> findByTenantAccountIdAndOrganizationId(
            Long tenantAccountId, Long organizationId);

    boolean existsByTenantAccountIdAndOrganizationId(Long tenantAccountId, Long organizationId);

    boolean existsByOrganizationId(Long organizationId);

    void deleteByTenantAccountIdAndOrganizationId(Long tenantAccountId, Long organizationId);

    void deleteByTenantAccountId(Long tenantAccountId);
}
