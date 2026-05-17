package iam.platform.auth.domain.repository;

import iam.platform.auth.domain.model.entity.RolePermission;

import java.util.List;

public interface RolePermissionRepository {
    RolePermission save(RolePermission rolePermission);

    List<RolePermission> findByRoleId(Long roleId);

    List<RolePermission> findByPermissionId(Long permissionId);

    boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId);

    void deleteByRoleIdAndPermissionId(Long roleId, Long permissionId);

    void deleteByRoleId(Long roleId);

    void deleteByPermissionId(Long permissionId);
}
