package iam.platform.admin.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import iam.platform.admin.infrastructure.persistence.entity.UserRoleMappingPO;

import java.util.List;

public interface UserRoleMappingJpaRepository extends JpaRepository<UserRoleMappingPO, Long> {
    List<UserRoleMappingPO> findByUserIdAndTenantId(Long userId, Long tenantId);

    List<UserRoleMappingPO> findByRoleId(Long roleId);

    boolean existsByUserIdAndTenantIdAndRoleId(Long userId, Long tenantId, Long roleId);
}
