package iam.platform.admin.infrastructure.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import iam.platform.admin.domain.model.entity.TenantAccountOrganizationMapping;
import iam.platform.admin.domain.repository.TenantAccountOrganizationMappingRepository;
import iam.platform.admin.infrastructure.persistence.converter.DomainPoMapper;
import iam.platform.admin.infrastructure.persistence.entity.TenantAccountOrganizationMappingPO;
import iam.platform.admin.infrastructure.persistence.repository.TenantAccountOrganizationMappingJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TenantAccountOrganizationMappingRepositoryImpl
        implements TenantAccountOrganizationMappingRepository {

    private final TenantAccountOrganizationMappingJpaRepository jpaRepository;
    private final DomainPoMapper domainPoMapper;

    @Override
    public TenantAccountOrganizationMapping save(TenantAccountOrganizationMapping mapping) {
        TenantAccountOrganizationMappingPO po =
                domainPoMapper.toTenantAccountOrganizationMappingPO(mapping);
        TenantAccountOrganizationMappingPO saved = jpaRepository.save(po);
        return domainPoMapper.toTenantAccountOrganizationMappingDomain(saved);
    }

    @Override
    public Optional<TenantAccountOrganizationMapping> findById(Long id) {
        return jpaRepository.findById(id)
                .map(domainPoMapper::toTenantAccountOrganizationMappingDomain);
    }

    @Override
    public List<TenantAccountOrganizationMapping> findByTenantAccountId(Long tenantAccountId) {
        return jpaRepository.findByTenantAccountId(tenantAccountId).stream()
                .map(domainPoMapper::toTenantAccountOrganizationMappingDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TenantAccountOrganizationMapping> findByOrganizationId(Long organizationId) {
        return jpaRepository.findByOrganizationId(organizationId).stream()
                .map(domainPoMapper::toTenantAccountOrganizationMappingDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<TenantAccountOrganizationMapping> findByTenantAccountIdAndOrganizationId(
            Long tenantAccountId, Long organizationId) {
        return jpaRepository.findByTenantAccountIdAndOrganizationId(tenantAccountId, organizationId)
                .map(domainPoMapper::toTenantAccountOrganizationMappingDomain);
    }

    @Override
    public boolean existsByTenantAccountIdAndOrganizationId(Long tenantAccountId,
            Long organizationId) {
        return jpaRepository.existsByTenantAccountIdAndOrganizationId(tenantAccountId,
                organizationId);
    }

    @Override
    public boolean existsByOrganizationId(Long organizationId) {
        return jpaRepository.existsByOrganizationId(organizationId);
    }

    @Override
    public void deleteByTenantAccountIdAndOrganizationId(Long tenantAccountId,
            Long organizationId) {
        jpaRepository.deleteByTenantAccountIdAndOrganizationId(tenantAccountId, organizationId);
    }

    @Override
    public void deleteByTenantAccountId(Long tenantAccountId) {
        jpaRepository.deleteByTenantAccountId(tenantAccountId);
    }

}
