package iam.platform.admin.domain.repository;

import iam.platform.admin.domain.model.entity.TenantMenuConfig;

import java.util.List;
import java.util.Optional;

public interface TenantMenuConfigRepository {
    TenantMenuConfig save(TenantMenuConfig config);

    Optional<TenantMenuConfig> findById(Long id);

    List<TenantMenuConfig> findByTenantId(Long tenantId);

    List<TenantMenuConfig> findByTenantIdAndEnabledTrue(Long tenantId);

    Optional<TenantMenuConfig> findByTenantIdAndMenuId(Long tenantId, Long menuId);

    boolean existsByTenantIdAndMenuId(Long tenantId, Long menuId);

    void deleteById(Long id);

    void deleteByTenantIdAndMenuId(Long tenantId, Long menuId);
}
