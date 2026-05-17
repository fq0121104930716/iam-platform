package iam.platform.admin.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import iam.platform.admin.infrastructure.persistence.entity.TenantPO;

import java.util.Optional;

public interface TenantJpaRepository extends JpaRepository<TenantPO, Long> {
    Optional<TenantPO> findByTenantCode(String tenantCode);

    boolean existsByTenantCode(String tenantCode);

    long countByStatus(String status);
}
