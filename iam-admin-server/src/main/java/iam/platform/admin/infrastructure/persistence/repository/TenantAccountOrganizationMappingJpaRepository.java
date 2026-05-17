package iam.platform.admin.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import iam.platform.admin.infrastructure.persistence.entity.TenantAccountOrganizationMappingPO;

import java.util.List;
import java.util.Optional;

public interface TenantAccountOrganizationMappingJpaRepository
                extends JpaRepository<TenantAccountOrganizationMappingPO, Long> {

        List<TenantAccountOrganizationMappingPO> findByTenantAccountId(Long tenantAccountId);

        List<TenantAccountOrganizationMappingPO> findByOrganizationId(Long organizationId);

        Optional<TenantAccountOrganizationMappingPO> findByTenantAccountIdAndOrganizationId(
                        Long tenantAccountId, Long organizationId);

        boolean existsByTenantAccountIdAndOrganizationId(Long tenantAccountId, Long organizationId);

        boolean existsByOrganizationId(Long organizationId);

        void deleteByTenantAccountIdAndOrganizationId(Long tenantAccountId, Long organizationId);

        void deleteByTenantAccountId(Long tenantAccountId);
}
