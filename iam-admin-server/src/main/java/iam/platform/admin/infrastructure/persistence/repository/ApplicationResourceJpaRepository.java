package iam.platform.admin.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import iam.platform.admin.infrastructure.persistence.entity.ApplicationResourcePO;

import java.util.List;
import java.util.Optional;

public interface ApplicationResourceJpaRepository extends JpaRepository<ApplicationResourcePO, Long> {
    Optional<ApplicationResourcePO> findByApplicationIdAndResourceCode(Long applicationId, String resourceCode);

    List<ApplicationResourcePO> findByApplicationIdOrderBySortOrderAsc(Long applicationId);

    List<ApplicationResourcePO> findByApplicationIdAndParentIdOrderBySortOrderAsc(Long applicationId, Long parentId);

    List<ApplicationResourcePO> findByApplicationIdAndResourceTypeOrderBySortOrderAsc(Long applicationId, String resourceType);

    boolean existsByApplicationIdAndResourceCode(Long applicationId, String resourceCode);
}
