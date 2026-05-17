package iam.platform.auth.infrastructure.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import iam.platform.auth.domain.model.entity.RolePermission;
import iam.platform.auth.domain.repository.RolePermissionRepository;
import iam.platform.auth.infrastructure.persistence.entity.RolePermissionPO;
import iam.platform.auth.infrastructure.persistence.repository.RolePermissionJpaRepository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RolePermissionRepositoryImpl implements RolePermissionRepository {

    private final RolePermissionJpaRepository jpaRepository;

    @Override
    public RolePermission save(RolePermission rolePermission) {
        RolePermissionPO po = toPO(rolePermission);
        po = jpaRepository.save(po);
        return toDomain(po);
    }

    @Override
    public List<RolePermission> findByRoleId(Long roleId) {
        return jpaRepository.findByRoleId(roleId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<RolePermission> findByPermissionId(Long permissionId) {
        return jpaRepository.findByPermissionId(permissionId).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByRoleIdAndPermissionId(Long roleId, Long permissionId) {
        return jpaRepository.existsByRoleIdAndPermissionId(roleId, permissionId);
    }

    @Override
    public void deleteByRoleIdAndPermissionId(Long roleId, Long permissionId) {
        jpaRepository.deleteByRoleIdAndPermissionId(roleId, permissionId);
    }

    @Override
    public void deleteByRoleId(Long roleId) {
        jpaRepository.deleteByRoleId(roleId);
    }

    @Override
    public void deleteByPermissionId(Long permissionId) {
        jpaRepository.deleteByPermissionId(permissionId);
    }

    private RolePermissionPO toPO(RolePermission rolePermission) {
        return new RolePermissionPO(rolePermission.getRoleId(), rolePermission.getPermissionId());
    }

    private RolePermission toDomain(RolePermissionPO po) {
        return RolePermission.builder().id(po.getId()).roleId(po.getRoleId())
                .permissionId(po.getPermissionId()).createdAt(po.getCreatedAt()).build();
    }
}
