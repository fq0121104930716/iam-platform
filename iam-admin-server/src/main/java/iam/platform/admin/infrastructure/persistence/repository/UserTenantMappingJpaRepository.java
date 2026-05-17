package iam.platform.admin.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import iam.platform.admin.infrastructure.persistence.entity.UserTenantMappingPO;

import java.util.List;
import java.util.Optional;

public interface UserTenantMappingJpaRepository extends JpaRepository<UserTenantMappingPO, Long> {
    Optional<UserTenantMappingPO> findByUserIdAndTenantId(Long userId, Long tenantId);

    List<UserTenantMappingPO> findByUserId(Long userId);

    List<UserTenantMappingPO> findByTenantId(Long tenantId);

    Page<UserTenantMappingPO> findByTenantId(Long tenantId, Pageable pageable);

    long countByTenantIdAndStatus(Long tenantId, String status);

    boolean existsByUserIdAndTenantId(Long userId, Long tenantId);
}
