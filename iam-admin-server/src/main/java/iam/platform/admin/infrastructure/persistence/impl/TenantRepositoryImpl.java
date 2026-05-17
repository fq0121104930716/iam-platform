package iam.platform.admin.infrastructure.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import iam.platform.admin.domain.model.entity.Tenant;
import iam.platform.admin.domain.repository.TenantRepository;
import iam.platform.admin.infrastructure.persistence.converter.DomainPoMapper;
import iam.platform.admin.infrastructure.persistence.entity.TenantPO;
import iam.platform.admin.infrastructure.persistence.repository.TenantJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TenantRepositoryImpl implements TenantRepository {

    private final TenantJpaRepository jpaRepository;
    private final DomainPoMapper domainPoMapper;

    @Override
    public Tenant save(Tenant tenant) {
        TenantPO po = domainPoMapper.toTenantPO(tenant);
        TenantPO savedPo = jpaRepository.save(po);
        return domainPoMapper.toTenantDomain(savedPo);
    }

    @Override
    public Optional<Tenant> findById(Long id) {
        return jpaRepository.findById(id).map(domainPoMapper::toTenantDomain);
    }

    @Override
    public Optional<Tenant> findByTenantCode(String tenantCode) {
        return jpaRepository.findByTenantCode(tenantCode).map(domainPoMapper::toTenantDomain);
    }

    @Override
    public Page<Tenant> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(domainPoMapper::toTenantDomain);
    }

    @Override
    public boolean existsByTenantCode(String tenantCode) {
        return jpaRepository.existsByTenantCode(tenantCode);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long countByStatus(String status) {
        return jpaRepository.countByStatus(status);
    }

    @Override
    public List<Tenant> findAllByStatus(String status) {
        return jpaRepository.findAll().stream().filter(po -> po.getStatus().equals(status))
                .map(domainPoMapper::toTenantDomain).collect(Collectors.toList());
    }
}
