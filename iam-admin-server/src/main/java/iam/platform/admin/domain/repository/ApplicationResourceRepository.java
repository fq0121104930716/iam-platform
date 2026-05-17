package iam.platform.admin.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import iam.platform.admin.domain.model.entity.ApplicationResource;

import java.util.List;
import java.util.Optional;

public interface ApplicationResourceRepository {
    ApplicationResource save(ApplicationResource resource);

    Optional<ApplicationResource> findById(Long id);

    Optional<ApplicationResource> findByApplicationIdAndResourceCode(Long applicationId, String resourceCode);

    List<ApplicationResource> findByApplicationIdOrderBySortOrder(Long applicationId);

    List<ApplicationResource> findByApplicationIdAndParentIdOrderBySortOrder(Long applicationId, Long parentId);

    List<ApplicationResource> findByApplicationIdAndResourceTypeOrderBySortOrder(Long applicationId, String resourceType);

    Page<ApplicationResource> findByApplicationId(Long applicationId, Pageable pageable);

    boolean existsByApplicationIdAndResourceCode(Long applicationId, String resourceCode);

    void deleteById(Long id);

    void deleteByApplicationId(Long applicationId);
}
