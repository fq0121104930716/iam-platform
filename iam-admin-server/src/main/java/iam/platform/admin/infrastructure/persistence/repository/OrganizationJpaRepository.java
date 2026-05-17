package iam.platform.admin.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import iam.platform.admin.infrastructure.persistence.entity.OrganizationPO;

import java.util.List;

public interface OrganizationJpaRepository extends JpaRepository<OrganizationPO, Long> {
    List<OrganizationPO> findByTenantId(Long tenantId);

    List<OrganizationPO> findByParentId(Long parentId);

    List<OrganizationPO> findByPathStartingWith(String pathPrefix);

    boolean existsByTenantIdAndOrgCode(Long tenantId, String orgCode);

    long countByTenantId(Long tenantId);
}
