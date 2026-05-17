package iam.platform.auth.domain.repository;

import iam.platform.auth.domain.model.entity.ResourcePermission;

import java.util.List;
import java.util.Optional;

public interface ResourcePermissionRepository {
    ResourcePermission save(ResourcePermission permission);

    Optional<ResourcePermission> findById(Long id);

    Optional<ResourcePermission> findByPermissionCode(String permissionCode);

    List<ResourcePermission> findByTenantId(Long tenantId);

    List<ResourcePermission> findGlobalPermissions();

    List<ResourcePermission> findByTenantIdOrGlobal(Long tenantId);

    List<ResourcePermission> findByResourceType(String resourceType);

    void deleteById(Long id);

    boolean existsByTenantIdAndPermissionCode(Long tenantId, String permissionCode);
}
