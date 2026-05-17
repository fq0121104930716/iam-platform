package iam.platform.admin.infrastructure.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import iam.platform.admin.domain.model.entity.Role;
import iam.platform.common.model.enums.RoleType;
import iam.platform.admin.domain.repository.RoleRepository;
import iam.platform.admin.infrastructure.persistence.entity.RolePO;
import iam.platform.admin.infrastructure.persistence.repository.RoleJpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoleRepositoryImpl implements RoleRepository {

    private final RoleJpaRepository jpaRepository;

    @Override
    public Role save(Role role) {
        RolePO po = toPO(role);
        po = jpaRepository.save(po);
        return toDomain(po);
    }

    @Override
    public Optional<Role> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Role> findByCode(String code) {
        return jpaRepository.findByCode(code).map(this::toDomain);
    }

    @Override
    public Optional<Role> findByTenantIdAndCode(Long tenantId, String code) {
        return jpaRepository.findByTenantId(tenantId).stream()
                .filter(po -> po.getCode().equals(code)).findFirst().map(this::toDomain);
    }

    @Override
    public List<Role> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Role> findByTenantId(Long tenantId) {
        return jpaRepository.findByTenantId(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Role> findGlobalRoles() {
        return jpaRepository.findByTenantIdIsNull().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Role> findByTenantIdOrGlobal(Long tenantId) {
        return jpaRepository.findByTenantIdOrTenantIdIsNull(tenantId).stream().map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByTenantIdAndCode(Long tenantId, String code) {
        return jpaRepository.existsByTenantIdAndCode(tenantId, code);
    }

    private RolePO toPO(Role role) {
        RolePO po = new RolePO(role.getTenantId(), role.getCode(), role.getName(),
                role.getRoleType() != null ? role.getRoleType() : RoleType.SYSTEM,
                role.getDescription(), role.getIsSystem() != null ? role.getIsSystem() : false);
        // If role has an ID, we need to update existing entity
        if (role.getId() != null) {
            Optional<RolePO> existing = jpaRepository.findById(role.getId());
            if (existing.isPresent()) {
                RolePO existingPo = existing.get();
                existingPo.setTenantId(role.getTenantId());
                existingPo.setCode(role.getCode());
                existingPo.setName(role.getName());
                existingPo.setRoleType(
                        role.getRoleType() != null ? role.getRoleType() : RoleType.SYSTEM);
                existingPo.setDescription(role.getDescription());
                existingPo.setIsSystem(role.getIsSystem() != null ? role.getIsSystem() : false);
                return existingPo;
            }
        }
        return po;
    }

    private Role toDomain(RolePO po) {
        return Role.builder().id(po.getId()).tenantId(po.getTenantId()).code(po.getCode())
                .name(po.getName()).roleType(po.getRoleType()).description(po.getDescription())
                .isSystem(po.getIsSystem()).createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt()).build();
    }
}
