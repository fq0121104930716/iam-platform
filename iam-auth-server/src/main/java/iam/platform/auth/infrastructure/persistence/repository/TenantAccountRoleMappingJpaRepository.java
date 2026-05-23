package iam.platform.auth.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import iam.platform.auth.infrastructure.persistence.entity.TenantAccountRoleMappingPO;

import java.util.List;

public interface TenantAccountRoleMappingJpaRepository
        extends JpaRepository<TenantAccountRoleMappingPO, Long> {

    List<TenantAccountRoleMappingPO> findByUserIdAndTenantId(Long userId, Long tenantId);

    List<TenantAccountRoleMappingPO> findByRoleId(Long roleId);

    boolean existsByUserIdAndTenantIdAndRoleId(Long userId, Long tenantId, Long roleId);

    void deleteByUserIdAndTenantIdAndRoleId(Long userId, Long tenantId, Long roleId);

    void deleteByUserIdAndTenantId(Long userId, Long tenantId);

    void deleteByRoleId(Long roleId);
}
