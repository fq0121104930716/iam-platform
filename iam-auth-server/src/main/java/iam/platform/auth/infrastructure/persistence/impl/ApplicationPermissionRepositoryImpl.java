package iam.platform.auth.infrastructure.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import iam.platform.auth.domain.model.entity.ApplicationPermission;
import iam.platform.common.model.enums.PermissionAction;
import iam.platform.auth.domain.repository.ApplicationPermissionRepository;
import iam.platform.auth.infrastructure.persistence.entity.ApplicationPermissionPO;
import iam.platform.auth.infrastructure.persistence.repository.ApplicationPermissionJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ApplicationPermissionRepositoryImpl implements ApplicationPermissionRepository {

    private final ApplicationPermissionJpaRepository jpaRepository;

    @Override
    public ApplicationPermission save(ApplicationPermission permission) {
        ApplicationPermissionPO po = toPO(permission);
        po = jpaRepository.save(po);
        return toDomain(po);
    }

    @Override
    public Optional<ApplicationPermission> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<ApplicationPermission> findByApplicationId(Long applicationId) {
        return jpaRepository.findByApplicationId(applicationId).stream().map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ApplicationPermission> findByApplicationIdAndPermissionCode(Long applicationId,
            String permissionCode) {
        return jpaRepository.findByApplicationIdAndPermissionCode(applicationId, permissionCode)
                .map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private ApplicationPermissionPO toPO(ApplicationPermission permission) {
        ApplicationPermissionPO po = new ApplicationPermissionPO();
        po.setId(permission.getId());
        po.setApplicationId(permission.getApplicationId());
        po.setPermissionCode(permission.getPermissionCode());
        po.setPermissionName(permission.getPermissionName());
        po.setResourceType(permission.getResourceType());
        po.setAction(permission.getAction() != null ? permission.getAction().name() : null);
        po.setDescription(permission.getDescription());
        po.setCreatedAt(permission.getCreatedAt());
        po.setUpdatedAt(permission.getUpdatedAt());
        return po;
    }

    private ApplicationPermission toDomain(ApplicationPermissionPO po) {
        PermissionAction action = po.getAction() != null
                ? PermissionAction.valueOf(po.getAction())
                : null;
        return ApplicationPermission.builder().id(po.getId()).applicationId(po.getApplicationId())
                .permissionCode(po.getPermissionCode()).permissionName(po.getPermissionName())
                .resourceType(po.getResourceType()).action(action)
                .description(po.getDescription()).createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt()).build();
    }
}
