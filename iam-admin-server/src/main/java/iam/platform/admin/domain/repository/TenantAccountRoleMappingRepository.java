package iam.platform.admin.domain.repository;

import iam.platform.admin.domain.model.entity.TenantAccountRoleMapping;

import java.util.List;
import java.util.Optional;

public interface TenantAccountRoleMappingRepository {
    TenantAccountRoleMapping save(TenantAccountRoleMapping mapping);

    Optional<TenantAccountRoleMapping> findById(Long id);

    List<TenantAccountRoleMapping> findByTenantAccountId(Long tenantAccountId);

    List<TenantAccountRoleMapping> findByRoleId(Long roleId);

    boolean existsByTenantAccountIdAndRoleId(Long tenantAccountId, Long roleId);

    void deleteByTenantAccountIdAndRoleId(Long tenantAccountId, Long roleId);

    void deleteByTenantAccountId(Long tenantAccountId);

    void deleteByRoleId(Long roleId);
}
