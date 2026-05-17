package iam.platform.admin.infrastructure.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import iam.platform.admin.domain.model.entity.ApplicationTenantMapping;
import iam.platform.admin.domain.repository.ApplicationTenantMappingRepository;
import iam.platform.admin.infrastructure.persistence.converter.DomainPoMapper;
import iam.platform.admin.infrastructure.persistence.entity.ApplicationTenantMappingPO;
import iam.platform.admin.infrastructure.persistence.repository.ApplicationTenantMappingJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ApplicationTenantMappingRepositoryImpl implements ApplicationTenantMappingRepository {

    private final ApplicationTenantMappingJpaRepository jpaRepository;
    private final DomainPoMapper domainPoMapper;

    @Override
    public ApplicationTenantMapping save(ApplicationTenantMapping mapping) {
        ApplicationTenantMappingPO po = domainPoMapper.toApplicationTenantMappingPO(mapping);
        ApplicationTenantMappingPO savedPo = jpaRepository.save(po);
        return domainPoMapper.toApplicationTenantMappingDomain(savedPo);
    }

    @Override
    public Optional<ApplicationTenantMapping> findById(Long id) {
        return jpaRepository.findById(id).map(domainPoMapper::toApplicationTenantMappingDomain);
    }

    @Override
    public List<ApplicationTenantMapping> findByTenantId(Long tenantId) {
        return jpaRepository.findByTenantId(tenantId).stream()
                .map(domainPoMapper::toApplicationTenantMappingDomain).collect(Collectors.toList());
    }

    @Override
    public List<ApplicationTenantMapping> findByTenantIdAndEnabledTrue(Long tenantId) {
        return jpaRepository.findByTenantIdAndEnabledTrue(tenantId).stream()
                .map(domainPoMapper::toApplicationTenantMappingDomain).collect(Collectors.toList());
    }

    @Override
    public List<ApplicationTenantMapping> findByApplicationId(Long applicationId) {
        return jpaRepository.findByApplicationId(applicationId).stream()
                .map(domainPoMapper::toApplicationTenantMappingDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<ApplicationTenantMapping> findByApplicationIdAndTenantId(Long applicationId, Long tenantId) {
        return jpaRepository.findByApplicationIdAndTenantId(applicationId, tenantId)
                .map(domainPoMapper::toApplicationTenantMappingDomain);
    }

    @Override
    public boolean existsByApplicationIdAndTenantId(Long applicationId, Long tenantId) {
        return jpaRepository.existsByApplicationIdAndTenantId(applicationId, tenantId);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteByApplicationIdAndTenantId(Long applicationId, Long tenantId) {
        jpaRepository.findByApplicationIdAndTenantId(applicationId, tenantId)
                .ifPresent(jpaRepository::delete);
    }
}
