package iam.platform.auth.infrastructure.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import iam.platform.auth.domain.model.entity.TenantAccount;
import iam.platform.auth.domain.repository.TenantAccountRepository;
import iam.platform.auth.infrastructure.persistence.converter.DomainPoMapper;
import iam.platform.auth.infrastructure.persistence.entity.TenantAccountPO;
import iam.platform.auth.infrastructure.persistence.repository.TenantAccountJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class TenantAccountRepositoryImpl implements TenantAccountRepository {

    private final TenantAccountJpaRepository jpaRepository;
    private final DomainPoMapper domainPoMapper;

    @Override
    public TenantAccount save(TenantAccount tenantAccount) {
        TenantAccountPO po = domainPoMapper.toTenantAccountPO(tenantAccount);
        TenantAccountPO savedPo = jpaRepository.save(po);
        return domainPoMapper.toTenantAccountDomain(savedPo);
    }

    @Override
    public Optional<TenantAccount> findById(Long id) {
        return jpaRepository.findById(id).map(domainPoMapper::toTenantAccountDomain);
    }

    @Override
    public Optional<TenantAccount> findByUserIdAndTenantId(Long userId, Long tenantId) {
        return jpaRepository.findByUserIdAndTenantId(userId, tenantId)
                .map(domainPoMapper::toTenantAccountDomain);
    }

    @Override
    public List<TenantAccount> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(domainPoMapper::toTenantAccountDomain).collect(Collectors.toList());
    }

    @Override
    public List<TenantAccount> findByTenantId(Long tenantId) {
        return jpaRepository.findByTenantId(tenantId).stream()
                .map(domainPoMapper::toTenantAccountDomain).collect(Collectors.toList());
    }

    @Override
    public Page<TenantAccount> findByTenantId(Long tenantId, Pageable pageable) {
        return jpaRepository.findByTenantId(tenantId, pageable)
                .map(domainPoMapper::toTenantAccountDomain);
    }

    @Override
    public boolean existsByTenantIdAndAccountCode(Long tenantId, String accountCode) {
        return jpaRepository.existsByTenantIdAndAccountCode(tenantId, accountCode);
    }

    @Override
    public boolean existsByTenantIdAndEmployeeNo(Long tenantId, String employeeNo) {
        return jpaRepository.existsByTenantIdAndEmployeeNo(tenantId, employeeNo);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long countByTenantIdAndStatus(Long tenantId, String status) {
        return jpaRepository.countByTenantIdAndStatus(tenantId, status);
    }

    @Override
    public long countByStatus(String status) {
        return jpaRepository.countByStatus(status);
    }

}
