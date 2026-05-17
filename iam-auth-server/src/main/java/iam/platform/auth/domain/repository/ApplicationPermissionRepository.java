package iam.platform.auth.domain.repository;

import iam.platform.auth.domain.model.entity.ApplicationPermission;

import java.util.List;
import java.util.Optional;

public interface ApplicationPermissionRepository {
    ApplicationPermission save(ApplicationPermission permission);

    Optional<ApplicationPermission> findById(Long id);

    List<ApplicationPermission> findByApplicationId(Long applicationId);

    Optional<ApplicationPermission> findByApplicationIdAndPermissionCode(Long applicationId,
            String permissionCode);

    void deleteById(Long id);
}
