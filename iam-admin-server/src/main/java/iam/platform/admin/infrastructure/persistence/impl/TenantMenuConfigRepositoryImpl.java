package iam.platform.admin.infrastructure.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import iam.platform.admin.domain.model.entity.TenantMenuConfig;
import iam.platform.admin.domain.repository.TenantMenuConfigRepository;
import iam.platform.admin.infrastructure.persistence.converter.DomainPoMapper;
import iam.platform.admin.infrastructure.persistence.entity.TenantMenuConfigPO;
import iam.platform.admin.infrastructure.persistence.repository.TenantMenuConfigJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TenantMenuConfigRepositoryImpl implements TenantMenuConfigRepository {

    private final TenantMenuConfigJpaRepository jpaRepository;
    private final DomainPoMapper domainPoMapper;

    @Override
    public TenantMenuConfig save(TenantMenuConfig config) {
        TenantMenuConfigPO po = domainPoMapper.toTenantMenuConfigPO(config);
        TenantMenuConfigPO savedPo = jpaRepository.save(po);
        return domainPoMapper.toTenantMenuConfigDomain(savedPo);
    }

    @Override
    public Optional<TenantMenuConfig> findById(Long id) {
        return jpaRepository.findById(id).map(domainPoMapper::toTenantMenuConfigDomain);
    }

    @Override
    public List<TenantMenuConfig> findByTenantId(Long tenantId) {
        return jpaRepository.findByTenantId(tenantId).stream()
                .map(domainPoMapper::toTenantMenuConfigDomain).collect(Collectors.toList());
    }

    @Override
    public List<TenantMenuConfig> findByTenantIdAndEnabledTrue(Long tenantId) {
        return jpaRepository.findByTenantIdAndEnabledTrue(tenantId).stream()
                .map(domainPoMapper::toTenantMenuConfigDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<TenantMenuConfig> findByTenantIdAndMenuId(Long tenantId, Long menuId) {
        return jpaRepository.findByTenantIdAndMenuId(tenantId, menuId)
                .map(domainPoMapper::toTenantMenuConfigDomain);
    }

    @Override
    public boolean existsByTenantIdAndMenuId(Long tenantId, Long menuId) {
        return jpaRepository.existsByTenantIdAndMenuId(tenantId, menuId);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteByTenantIdAndMenuId(Long tenantId, Long menuId) {
        jpaRepository.findByTenantIdAndMenuId(tenantId, menuId).ifPresent(jpaRepository::delete);
    }
}
