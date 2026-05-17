package iam.platform.admin.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import iam.platform.admin.domain.model.entity.Tenant;

import java.util.List;
import java.util.Optional;

public interface TenantRepository {
    Tenant save(Tenant tenant);

    Optional<Tenant> findById(Long id);

    Optional<Tenant> findByTenantCode(String tenantCode);

    Page<Tenant> findAll(Pageable pageable);

    boolean existsByTenantCode(String tenantCode);

    void deleteById(Long id);

    long countByStatus(String status);

    List<Tenant> findAllByStatus(String status);
}
