package iam.platform.admin.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import iam.platform.admin.infrastructure.persistence.entity.ApplicationPermissionPO;

import java.util.List;
import java.util.Optional;

public interface ApplicationPermissionJpaRepository
        extends JpaRepository<ApplicationPermissionPO, Long> {
    List<ApplicationPermissionPO> findByApplicationId(Long applicationId);

    Optional<ApplicationPermissionPO> findByApplicationIdAndPermissionCode(Long applicationId,
            String permissionCode);
}
