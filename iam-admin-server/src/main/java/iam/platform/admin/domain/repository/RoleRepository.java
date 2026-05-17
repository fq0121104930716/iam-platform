package iam.platform.admin.domain.repository;

import iam.platform.admin.domain.model.entity.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepository {
    Role save(Role role);

    Optional<Role> findById(Long id);

    Optional<Role> findByCode(String code);

    Optional<Role> findByTenantIdAndCode(Long tenantId, String code);

    List<Role> findAll();

    List<Role> findByTenantId(Long tenantId);

    List<Role> findGlobalRoles();

    List<Role> findByTenantIdOrGlobal(Long tenantId);

    void deleteById(Long id);

    boolean existsByTenantIdAndCode(Long tenantId, String code);
}
