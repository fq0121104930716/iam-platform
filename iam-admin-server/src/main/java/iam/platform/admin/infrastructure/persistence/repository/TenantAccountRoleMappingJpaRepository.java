package iam.platform.admin.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import iam.platform.admin.infrastructure.persistence.entity.TenantAccountRoleMappingPO;

import java.util.List;

public interface TenantAccountRoleMappingJpaRepository
        extends JpaRepository<TenantAccountRoleMappingPO, Long> {

    List<TenantAccountRoleMappingPO> findByTenantAccountId(Long tenantAccountId);

    List<TenantAccountRoleMappingPO> findByRoleId(Long roleId);

    boolean existsByTenantAccountIdAndRoleId(Long tenantAccountId, Long roleId);

    void deleteByTenantAccountIdAndRoleId(Long tenantAccountId, Long roleId);

    void deleteByTenantAccountId(Long tenantAccountId);

    void deleteByRoleId(Long roleId);
}
