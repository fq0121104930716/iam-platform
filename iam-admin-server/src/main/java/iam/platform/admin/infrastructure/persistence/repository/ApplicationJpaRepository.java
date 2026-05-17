package iam.platform.admin.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import iam.platform.admin.infrastructure.persistence.entity.ApplicationPO;

import java.util.List;
import java.util.Optional;

public interface ApplicationJpaRepository extends JpaRepository<ApplicationPO, Long> {
    Optional<ApplicationPO> findByAppId(String appId);

    List<ApplicationPO> findByTenantId(Long tenantId);

    long countByTenantIdAndStatus(Long tenantId, String status);

    long countByStatus(String status);
}
