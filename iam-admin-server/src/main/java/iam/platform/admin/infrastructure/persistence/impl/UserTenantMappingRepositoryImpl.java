package iam.platform.admin.infrastructure.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import iam.platform.admin.domain.model.entity.UserTenantMapping;
import iam.platform.admin.domain.repository.UserTenantMappingRepository;
import iam.platform.admin.infrastructure.persistence.converter.DomainPoMapper;
import iam.platform.admin.infrastructure.persistence.entity.UserTenantMappingPO;
import iam.platform.admin.infrastructure.persistence.repository.UserTenantMappingJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserTenantMappingRepositoryImpl implements UserTenantMappingRepository {

    private final UserTenantMappingJpaRepository jpaRepository;
    private final DomainPoMapper domainPoMapper;

    @Override
    public UserTenantMapping save(UserTenantMapping mapping) {
        UserTenantMappingPO po = domainPoMapper.toUserTenantMappingPO(mapping);
        UserTenantMappingPO savedPo = jpaRepository.save(po);
        return domainPoMapper.toUserTenantMappingDomain(savedPo);
    }

    @Override
    public Optional<UserTenantMapping> findById(Long id) {
        return jpaRepository.findById(id).map(domainPoMapper::toUserTenantMappingDomain);
    }

    @Override
    public Optional<UserTenantMapping> findByUserIdAndTenantId(Long userId, Long tenantId) {
        return jpaRepository.findByUserIdAndTenantId(userId, tenantId)
                .map(domainPoMapper::toUserTenantMappingDomain);
    }

    @Override
    public List<UserTenantMapping> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(domainPoMapper::toUserTenantMappingDomain).collect(Collectors.toList());
    }

    @Override
    public List<UserTenantMapping> findByTenantId(Long tenantId) {
        return jpaRepository.findByTenantId(tenantId).stream()
                .map(domainPoMapper::toUserTenantMappingDomain).collect(Collectors.toList());
    }

    @Override
    public Page<UserTenantMapping> findByTenantId(Long tenantId, Pageable pageable) {
        return jpaRepository.findByTenantId(tenantId, pageable)
                .map(domainPoMapper::toUserTenantMappingDomain);
    }

    @Override
    public long countByTenantIdAndStatus(Long tenantId, String status) {
        return jpaRepository.countByTenantIdAndStatus(tenantId, status);
    }

    @Override
    public boolean existsByUserIdAndTenantId(Long userId, Long tenantId) {
        return jpaRepository.existsByUserIdAndTenantId(userId, tenantId);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
