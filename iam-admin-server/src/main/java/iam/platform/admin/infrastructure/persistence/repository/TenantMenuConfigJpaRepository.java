package iam.platform.admin.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import iam.platform.admin.infrastructure.persistence.entity.TenantMenuConfigPO;

import java.util.List;
import java.util.Optional;

public interface TenantMenuConfigJpaRepository extends JpaRepository<TenantMenuConfigPO, Long> {
    List<TenantMenuConfigPO> findByTenantId(Long tenantId);

    List<TenantMenuConfigPO> findByTenantIdAndEnabledTrue(Long tenantId);

    Optional<TenantMenuConfigPO> findByTenantIdAndMenuId(Long tenantId, Long menuId);

    boolean existsByTenantIdAndMenuId(Long tenantId, Long menuId);
}
