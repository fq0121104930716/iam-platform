package iam.platform.admin.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import iam.platform.admin.infrastructure.persistence.entity.ApplicationTenantMappingPO;

import java.util.List;
import java.util.Optional;

public interface ApplicationTenantMappingJpaRepository extends JpaRepository<ApplicationTenantMappingPO, Long> {
    List<ApplicationTenantMappingPO> findByTenantId(Long tenantId);

    List<ApplicationTenantMappingPO> findByTenantIdAndEnabledTrue(Long tenantId);

    List<ApplicationTenantMappingPO> findByApplicationId(Long applicationId);

    Optional<ApplicationTenantMappingPO> findByApplicationIdAndTenantId(Long applicationId, Long tenantId);

    boolean existsByApplicationIdAndTenantId(Long applicationId, Long tenantId);
}
