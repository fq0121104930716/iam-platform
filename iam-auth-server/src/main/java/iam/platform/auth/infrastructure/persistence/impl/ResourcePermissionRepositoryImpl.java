package iam.platform.auth.infrastructure.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import iam.platform.auth.domain.model.entity.ResourcePermission;
import iam.platform.auth.domain.repository.ResourcePermissionRepository;
import iam.platform.auth.infrastructure.persistence.entity.ResourcePermissionPO;
import iam.platform.auth.infrastructure.persistence.repository.ResourcePermissionJpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ResourcePermissionRepositoryImpl implements ResourcePermissionRepository {

    private final ResourcePermissionJpaRepository jpaRepository;

    @Override
    public ResourcePermission save(ResourcePermission permission) {
        ResourcePermissionPO po = toPO(permission);
        po = jpaRepository.save(po);
        return toDomain(po);
    }

    @Override
    public Optional<ResourcePermission> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<ResourcePermission> findByPermissionCode(String permissionCode) {
        return jpaRepository.findByPermissionCode(permissionCode).map(this::toDomain);
    }

    @Override
    public List<ResourcePermission> findByTenantId(Long tenantId) {
        return jpaRepository.findByTenantId(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<ResourcePermission> findGlobalPermissions() {
        return jpaRepository.findByTenantIdIsNull().stream().map(this::toDomain).toList();
    }

    @Override
    public List<ResourcePermission> findByTenantIdOrGlobal(Long tenantId) {
        return jpaRepository.findByTenantIdOrTenantIdIsNull(tenantId).stream().map(this::toDomain)
                .toList();
    }

    @Override
    public List<ResourcePermission> findByResourceType(String resourceType) {
        return jpaRepository.findByResourceType(resourceType).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByTenantIdAndPermissionCode(Long tenantId, String permissionCode) {
        return jpaRepository.existsByTenantIdAndPermissionCode(tenantId, permissionCode);
    }

    private ResourcePermissionPO toPO(ResourcePermission permission) {
        return new ResourcePermissionPO(permission.getTenantId(), permission.getPermissionCode(),
                permission.getPermissionName(), permission.getResourceType(),
                permission.getAction(), permission.getDescription());
    }

    private ResourcePermission toDomain(ResourcePermissionPO po) {
        return ResourcePermission.builder().id(po.getId()).tenantId(po.getTenantId())
                .permissionCode(po.getPermissionCode()).permissionName(po.getPermissionName())
                .resourceType(po.getResourceType()).action(po.getAction())
                .description(po.getDescription()).createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt()).build();
    }
}
