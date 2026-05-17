package iam.platform.admin.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import iam.platform.admin.domain.model.entity.UserRoleMapping;

import java.util.List;
import java.util.Optional;

public interface UserRoleMappingRepository {
    UserRoleMapping save(UserRoleMapping mapping);

    Optional<UserRoleMapping> findById(Long id);

    List<UserRoleMapping> findByUserIdAndTenantId(Long userId, Long tenantId);

    List<UserRoleMapping> findByRoleId(Long roleId);

    Page<UserRoleMapping> findByTenantId(Long tenantId, Pageable pageable);

    boolean existsByUserIdAndTenantIdAndRoleId(Long userId, Long tenantId, Long roleId);

    void deleteById(Long id);

    void deleteByUserIdAndTenantIdAndRoleId(Long userId, Long tenantId, Long roleId);
}
