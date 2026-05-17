package iam.platform.admin.domain.repository;

import iam.platform.admin.domain.model.entity.ApplicationTenantMapping;

import java.util.List;
import java.util.Optional;

public interface ApplicationTenantMappingRepository {
    ApplicationTenantMapping save(ApplicationTenantMapping mapping);

    Optional<ApplicationTenantMapping> findById(Long id);

    List<ApplicationTenantMapping> findByTenantId(Long tenantId);

    List<ApplicationTenantMapping> findByTenantIdAndEnabledTrue(Long tenantId);

    List<ApplicationTenantMapping> findByApplicationId(Long applicationId);

    Optional<ApplicationTenantMapping> findByApplicationIdAndTenantId(Long applicationId, Long tenantId);

    boolean existsByApplicationIdAndTenantId(Long applicationId, Long tenantId);

    void deleteById(Long id);

    void deleteByApplicationIdAndTenantId(Long applicationId, Long tenantId);
}
