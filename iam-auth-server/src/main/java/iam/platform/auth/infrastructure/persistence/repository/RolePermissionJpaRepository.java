package iam.platform.auth.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import iam.platform.auth.infrastructure.persistence.entity.RolePermissionPO;

import java.util.List;

public interface RolePermissionJpaRepository extends JpaRepository<RolePermissionPO, Long> {
    List<RolePermissionPO> findByRoleId(Long roleId);

    List<RolePermissionPO> findByPermissionId(Long permissionId);

    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);

    void deleteByRoleIdAndPermissionId(Long roleId, Long permissionId);

    void deleteByRoleId(Long roleId);

    void deleteByPermissionId(Long permissionId);
}
