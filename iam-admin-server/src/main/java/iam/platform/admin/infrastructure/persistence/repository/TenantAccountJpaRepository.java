package iam.platform.admin.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import iam.platform.admin.infrastructure.persistence.entity.TenantAccountPO;

import java.util.List;
import java.util.Optional;

public interface TenantAccountJpaRepository extends JpaRepository<TenantAccountPO, Long> {
    Optional<TenantAccountPO> findByPersonIdAndTenantId(Long personId, Long tenantId);

    List<TenantAccountPO> findByPersonId(Long personId);

    List<TenantAccountPO> findByTenantId(Long tenantId);

    Page<TenantAccountPO> findByTenantId(Long tenantId, Pageable pageable);

    boolean existsByTenantIdAndAccountCode(Long tenantId, String accountCode);

    boolean existsByTenantIdAndEmployeeNo(Long tenantId, String employeeNo);

    long countByTenantIdAndStatus(Long tenantId, String status);

    long countByStatus(String status);
}
