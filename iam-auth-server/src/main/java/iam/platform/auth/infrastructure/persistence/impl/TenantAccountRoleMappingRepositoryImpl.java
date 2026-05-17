package iam.platform.auth.infrastructure.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import iam.platform.auth.domain.model.entity.TenantAccountRoleMapping;
import iam.platform.auth.domain.repository.TenantAccountRoleMappingRepository;
import iam.platform.auth.infrastructure.persistence.converter.DomainPoMapper;
import iam.platform.auth.infrastructure.persistence.entity.TenantAccountRoleMappingPO;
import iam.platform.auth.infrastructure.persistence.repository.TenantAccountRoleMappingJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TenantAccountRoleMappingRepositoryImpl implements TenantAccountRoleMappingRepository {

    private final TenantAccountRoleMappingJpaRepository jpaRepository;
    private final DomainPoMapper domainPoMapper;

    @Override
    public TenantAccountRoleMapping save(TenantAccountRoleMapping mapping) {
        TenantAccountRoleMappingPO po = domainPoMapper.toTenantAccountRoleMappingPO(mapping);
        TenantAccountRoleMappingPO saved = jpaRepository.save(po);
        return domainPoMapper.toTenantAccountRoleMappingDomain(saved);
    }

    @Override
    public Optional<TenantAccountRoleMapping> findById(Long id) {
        return jpaRepository.findById(id).map(domainPoMapper::toTenantAccountRoleMappingDomain);
    }

    @Override
    public List<TenantAccountRoleMapping> findByTenantAccountId(Long tenantAccountId) {
        return jpaRepository.findByTenantAccountId(tenantAccountId).stream()
                .map(domainPoMapper::toTenantAccountRoleMappingDomain).collect(Collectors.toList());
    }

    @Override
    public List<TenantAccountRoleMapping> findByRoleId(Long roleId) {
        return jpaRepository.findByRoleId(roleId).stream()
                .map(domainPoMapper::toTenantAccountRoleMappingDomain).collect(Collectors.toList());
    }

    @Override
    public boolean existsByTenantAccountIdAndRoleId(Long tenantAccountId, Long roleId) {
        return jpaRepository.existsByTenantAccountIdAndRoleId(tenantAccountId, roleId);
    }

    @Override
    public void deleteByTenantAccountIdAndRoleId(Long tenantAccountId, Long roleId) {
        jpaRepository.deleteByTenantAccountIdAndRoleId(tenantAccountId, roleId);
    }

    @Override
    public void deleteByTenantAccountId(Long tenantAccountId) {
        jpaRepository.deleteByTenantAccountId(tenantAccountId);
    }

    @Override
    public void deleteByRoleId(Long roleId) {
        jpaRepository.deleteByRoleId(roleId);
    }

}
