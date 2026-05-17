package iam.platform.admin.infrastructure.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import iam.platform.admin.domain.model.entity.UserRoleMapping;
import iam.platform.admin.domain.repository.UserRoleMappingRepository;
import iam.platform.admin.infrastructure.persistence.converter.DomainPoMapper;
import iam.platform.admin.infrastructure.persistence.entity.UserRoleMappingPO;
import iam.platform.admin.infrastructure.persistence.repository.UserRoleMappingJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserRoleMappingRepositoryImpl implements UserRoleMappingRepository {

    private final UserRoleMappingJpaRepository jpaRepository;
    private final DomainPoMapper domainPoMapper;

    @Override
    public UserRoleMapping save(UserRoleMapping mapping) {
        UserRoleMappingPO po = domainPoMapper.toUserRoleMappingPO(mapping);
        UserRoleMappingPO savedPo = jpaRepository.save(po);
        return domainPoMapper.toUserRoleMappingDomain(savedPo);
    }

    @Override
    public Optional<UserRoleMapping> findById(Long id) {
        return jpaRepository.findById(id).map(domainPoMapper::toUserRoleMappingDomain);
    }

    @Override
    public List<UserRoleMapping> findByUserIdAndTenantId(Long userId, Long tenantId) {
        return jpaRepository.findByUserIdAndTenantId(userId, tenantId).stream()
                .map(domainPoMapper::toUserRoleMappingDomain).collect(Collectors.toList());
    }

    @Override
    public List<UserRoleMapping> findByRoleId(Long roleId) {
        return jpaRepository.findByRoleId(roleId).stream()
                .map(domainPoMapper::toUserRoleMappingDomain).collect(Collectors.toList());
    }

    @Override
    public Page<UserRoleMapping> findByTenantId(Long tenantId, Pageable pageable) {
        return jpaRepository.findAll(pageable).map(domainPoMapper::toUserRoleMappingDomain);
    }

    @Override
    public boolean existsByUserIdAndTenantIdAndRoleId(Long userId, Long tenantId, Long roleId) {
        return jpaRepository.existsByUserIdAndTenantIdAndRoleId(userId, tenantId, roleId);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteByUserIdAndTenantIdAndRoleId(Long userId, Long tenantId, Long roleId) {
        jpaRepository.findAll().stream()
                .filter(po -> po.getUserId().equals(userId) && po.getTenantId().equals(tenantId)
                        && po.getRoleId().equals(roleId))
                .findFirst().ifPresent(po -> jpaRepository.delete(po));
    }
}
