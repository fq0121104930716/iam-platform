package iam.platform.admin.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import iam.platform.admin.infrastructure.persistence.entity.RolePO;

import java.util.List;
import java.util.Optional;

public interface RoleJpaRepository extends JpaRepository<RolePO, Long> {
    Optional<RolePO> findByCode(String code);

    List<RolePO> findByTenantId(Long tenantId);

    List<RolePO> findByTenantIdIsNull();

    List<RolePO> findByTenantIdOrTenantIdIsNull(Long tenantId);

    boolean existsByTenantIdAndCode(Long tenantId, String code);

    boolean existsByCode(String code);
}
