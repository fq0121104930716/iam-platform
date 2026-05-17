package iam.platform.admin.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import iam.platform.admin.infrastructure.persistence.entity.ResourcePermissionPO;

import java.util.List;
import java.util.Optional;

public interface ResourcePermissionJpaRepository extends JpaRepository<ResourcePermissionPO, Long> {
    Optional<ResourcePermissionPO> findByPermissionCode(String permissionCode);

    List<ResourcePermissionPO> findByTenantId(Long tenantId);

    List<ResourcePermissionPO> findByTenantIdIsNull();

    List<ResourcePermissionPO> findByResourceType(String resourceType);

    List<ResourcePermissionPO> findByTenantIdOrTenantIdIsNull(Long tenantId);

    boolean existsByTenantIdAndPermissionCode(Long tenantId, String permissionCode);
}
