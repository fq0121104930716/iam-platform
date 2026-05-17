package iam.platform.admin.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import iam.platform.admin.domain.model.entity.UserTenantMapping;

import java.util.List;
import java.util.Optional;

public interface UserTenantMappingRepository {
    UserTenantMapping save(UserTenantMapping mapping);

    Optional<UserTenantMapping> findById(Long id);

    Optional<UserTenantMapping> findByUserIdAndTenantId(Long userId, Long tenantId);

    List<UserTenantMapping> findByUserId(Long userId);

    List<UserTenantMapping> findByTenantId(Long tenantId);

    Page<UserTenantMapping> findByTenantId(Long tenantId, Pageable pageable);

    long countByTenantIdAndStatus(Long tenantId, String status);

    boolean existsByUserIdAndTenantId(Long userId, Long tenantId);

    void deleteById(Long id);
}
