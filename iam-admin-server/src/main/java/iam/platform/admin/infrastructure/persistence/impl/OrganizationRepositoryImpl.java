package iam.platform.admin.infrastructure.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import iam.platform.admin.domain.model.entity.Organization;
import iam.platform.common.model.enums.OrgStatus;
import iam.platform.common.model.enums.OrgType;
import iam.platform.admin.domain.repository.OrganizationRepository;
import iam.platform.admin.infrastructure.persistence.entity.OrganizationPO;
import iam.platform.admin.infrastructure.persistence.repository.OrganizationJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrganizationRepositoryImpl implements OrganizationRepository {

    private final OrganizationJpaRepository jpaRepository;

    @Override
    public Organization save(Organization organization) {
        OrganizationPO po = toPO(organization);
        OrganizationPO savedPo = jpaRepository.save(po);
        return toDomain(savedPo);
    }

    @Override
    public Optional<Organization> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Organization> findByTenantId(Long tenantId) {
        return jpaRepository.findByTenantId(tenantId).stream().map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Organization> findByParentId(Long parentId) {
        return jpaRepository.findByParentId(parentId).stream().map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Organization> findByPathStartingWith(String pathPrefix) {
        return jpaRepository.findByPathStartingWith(pathPrefix).stream().map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByTenantIdAndOrgCode(Long tenantId, String orgCode) {
        return jpaRepository.existsByTenantIdAndOrgCode(tenantId, orgCode);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long countByTenantId(Long tenantId) {
        return jpaRepository.countByTenantId(tenantId);
    }

    private OrganizationPO toPO(Organization org) {
        OrganizationPO po = new OrganizationPO();
        po.setId(org.getId());
        po.setTenantId(org.getTenantId());
        po.setOrgCode(org.getOrgCode());
        po.setOrgName(org.getOrgName());
        po.setOrgType(org.getOrgType() != null ? org.getOrgType().name() : "DEPARTMENT");
        po.setParentId(org.getParentId());
        po.setLevel(org.getLevel());
        po.setPath(org.getPath());
        po.setSortOrder(org.getSortOrder());
        po.setManagerId(org.getManagerId());
        po.setPhone(org.getPhone());
        po.setEmail(org.getEmail());
        po.setStatus(org.getStatus() != null ? org.getStatus().name() : "ACTIVE");
        po.setDescription(org.getDescription());
        po.setCreatedAt(org.getCreatedAt());
        po.setUpdatedAt(org.getUpdatedAt());
        return po;
    }

    private Organization toDomain(OrganizationPO po) {
        return Organization.builder().id(po.getId()).tenantId(po.getTenantId())
                .orgCode(po.getOrgCode()).orgName(po.getOrgName())
                .orgType(OrgType.valueOf(po.getOrgType())).parentId(po.getParentId())
                .level(po.getLevel()).path(po.getPath()).sortOrder(po.getSortOrder())
                .managerId(po.getManagerId()).phone(po.getPhone()).email(po.getEmail())
                .status(OrgStatus.valueOf(po.getStatus())).description(po.getDescription())
                .createdAt(po.getCreatedAt()).updatedAt(po.getUpdatedAt()).build();
    }
}
