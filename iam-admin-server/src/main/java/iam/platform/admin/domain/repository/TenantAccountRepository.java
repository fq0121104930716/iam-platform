package iam.platform.admin.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import iam.platform.admin.domain.model.entity.TenantAccount;

import java.util.List;
import java.util.Optional;

public interface TenantAccountRepository {
    TenantAccount save(TenantAccount tenantAccount);

    Optional<TenantAccount> findById(Long id);

    Optional<TenantAccount> findByUserIdAndTenantId(Long userId, Long tenantId);

    List<TenantAccount> findByUserId(Long userId);

    List<TenantAccount> findByTenantId(Long tenantId);

    Page<TenantAccount> findByTenantId(Long tenantId, Pageable pageable);

    boolean existsByTenantIdAndAccountCode(Long tenantId, String accountCode);

    boolean existsByTenantIdAndEmployeeNo(Long tenantId, String employeeNo);

    void deleteById(Long id);

    long countByTenantIdAndStatus(Long tenantId, String status);

    long countByStatus(String status);
}
