package iam.platform.admin.domain.repository;

import iam.platform.admin.domain.model.entity.Organization;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository {
    Organization save(Organization organization);

    Optional<Organization> findById(Long id);

    List<Organization> findByTenantId(Long tenantId);

    List<Organization> findByParentId(Long parentId);

    List<Organization> findByPathStartingWith(String pathPrefix);

    boolean existsByTenantIdAndOrgCode(Long tenantId, String orgCode);

    void deleteById(Long id);

    long countByTenantId(Long tenantId);
}
