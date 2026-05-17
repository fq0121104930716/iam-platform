package iam.platform.admin.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import iam.platform.common.dto.request.CreateOrganizationRequest;
import iam.platform.common.dto.request.UpdateOrganizationRequest;
import iam.platform.common.dto.response.OrganizationResponse;
import iam.platform.common.model.annotation.AuditLog;
import iam.platform.admin.domain.model.entity.Organization;
import iam.platform.common.model.enums.AuditEventType;
import iam.platform.common.model.exception.ConflictException;
import iam.platform.common.model.exception.OrganizationNotFoundException;
import iam.platform.common.model.valueobject.OrganizationPath;
import iam.platform.admin.domain.repository.OrganizationRepository;
import iam.platform.admin.domain.service.OrganizationHierarchyService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationApplicationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationHierarchyService organizationHierarchyService;

    @Transactional
    @AuditLog(value = AuditEventType.ORGANIZATION_CREATED, resourceType = "organization", action = "创建组织 #{#request.orgName}")
    public OrganizationResponse createOrganization(Long tenantId,
            CreateOrganizationRequest request) {
        if (organizationRepository.existsByTenantIdAndOrgCode(tenantId, request.getOrgCode())) {
            throw new ConflictException(
                    "Organization code already exists in this tenant: " + request.getOrgCode());
        }

        Organization org;
        if (request.getParentId() != null) {
            Organization parent = organizationRepository.findById(request.getParentId())
                    .orElseThrow(() -> new OrganizationNotFoundException(
                            "Parent organization not found: " + request.getParentId()));

            // Domain factory method handles validation and hierarchy
            org = Organization.createChild(tenantId, request.getOrgCode(), request.getOrgName(),
                    request.getOrgType(), parent, request.getManagerId(), request.getSortOrder(),
                    request.getPhone(), request.getEmail(), request.getDescription());
        } else {
            org = Organization.createRoot(tenantId, request.getOrgCode(), request.getOrgName(),
                    request.getOrgType(), request.getManagerId(), request.getSortOrder(),
                    request.getPhone(), request.getEmail(), request.getDescription());
        }

        org = organizationRepository.save(org);

        // Fix path with the actual persisted ID
        org.fixPathAfterPersist(org.getId());
        org = organizationRepository.save(org);

        log.info("Organization created: {} (code: {}, tenantId: {})", org.getOrgName(),
                org.getOrgCode(), tenantId);
        return toResponse(org, null);
    }

    public OrganizationResponse getOrganization(Long id) {
        Organization org = organizationRepository.findById(id).orElseThrow(
                () -> new OrganizationNotFoundException("Organization not found: " + id));
        return toResponse(org, null);
    }

    @Transactional
    @AuditLog(value = AuditEventType.ORGANIZATION_UPDATED, resourceType = "organization", action = "更新组织 ID=#{#id}")
    public OrganizationResponse updateOrganization(Long tenantId, Long id,
            UpdateOrganizationRequest request) {
        Organization org = organizationRepository.findById(id).orElseThrow(
                () -> new OrganizationNotFoundException("Organization not found: " + id));

        // Domain entity enforces tenant ownership
        org.ensureBelongsToTenant(tenantId);

        // Delegate property updates to domain method
        org.updateInfo(request.getOrgName(), request.getOrgType(), request.getManagerId(),
                request.getSortOrder(), request.getPhone(), request.getEmail(),
                request.getDescription());

        // Handle reparenting if parent changed
        if (request.getParentId() != null && !request.getParentId().equals(org.getParentId())) {
            Organization newParent = organizationRepository.findById(request.getParentId())
                    .orElseThrow(() -> new OrganizationNotFoundException(
                            "Parent organization not found: " + request.getParentId()));

            // Domain service validates hierarchy constraints
            organizationHierarchyService.validateNotDescendant(org, newParent);

            OrganizationPath oldPath = org.getOrganizationPath();

            // Domain entity handles reparent logic
            org.reparent(newParent);
            org = organizationRepository.save(org);

            // Domain service updates children paths
            organizationHierarchyService.updateChildrenPaths(
                    tenantId, oldPath, org.getOrganizationPath());
        }

        org = organizationRepository.save(org);
        log.info("Organization updated: {}", org.getOrgCode());
        return toResponse(org, null);
    }

    @Transactional
    @AuditLog(value = AuditEventType.ORGANIZATION_DELETED, resourceType = "organization", action = "删除组织 ID=#{#id}")
    public void deleteOrganization(Long tenantId, Long id) {
        Organization org = organizationRepository.findById(id).orElseThrow(
                () -> new OrganizationNotFoundException("Organization not found: " + id));

        org.ensureBelongsToTenant(tenantId);

        List<Organization> children = organizationRepository.findByParentId(id);
        if (!children.isEmpty()) {
            throw new ConflictException("Cannot delete organization with child organizations.");
        }

        organizationRepository.deleteById(id);
        log.info("Organization deleted: {}", id);
    }

    @Transactional
    public void activateOrganization(Long tenantId, Long id) {
        Organization org = organizationRepository.findById(id).orElseThrow(
                () -> new OrganizationNotFoundException("Organization not found: " + id));

        org.ensureBelongsToTenant(tenantId);
        org.activate();
        organizationRepository.save(org);
        log.info("Organization activated: {}", id);
    }

    @Transactional
    public void deactivateOrganization(Long tenantId, Long id) {
        Organization org = organizationRepository.findById(id).orElseThrow(
                () -> new OrganizationNotFoundException("Organization not found: " + id));

        org.ensureBelongsToTenant(tenantId);
        org.deactivate();
        organizationRepository.save(org);
        log.info("Organization deactivated: {}", id);
    }

    public List<OrganizationResponse> getOrganizationTree(Long tenantId) {
        List<Organization> orgs = organizationRepository.findByTenantId(tenantId);
        return buildOrganizationTree(orgs);
    }

    private List<OrganizationResponse> buildOrganizationTree(List<Organization> orgs) {
        Map<Long, List<Organization>> orgsByParent = orgs.stream().collect(
                Collectors.groupingBy(org -> org.getParentId() != null ? org.getParentId() : -1L));

        return orgsByParent.getOrDefault(-1L, List.of()).stream()
                .map(org -> buildNode(org, orgsByParent)).toList();
    }

    private OrganizationResponse buildNode(Organization org,
            Map<Long, List<Organization>> orgsByParent) {
        OrganizationResponse response = toResponse(org, 0);
        List<Organization> children = orgsByParent.getOrDefault(org.getId(), List.of());
        if (!children.isEmpty()) {
            response.setChildren(
                    children.stream().map(child -> buildNode(child, orgsByParent)).toList());
        } else {
            response.setChildren(new ArrayList<>());
        }
        return response;
    }

    private OrganizationResponse toResponse(Organization org, Integer memberCount) {
        return OrganizationResponse.builder().id(org.getId()).tenantId(org.getTenantId())
                .orgCode(org.getOrgCode()).orgName(org.getOrgName()).orgType(org.getOrgType())
                .parentId(org.getParentId()).level(org.getLevel()).path(org.getPath())
                .sortOrder(org.getSortOrder()).managerId(org.getManagerId()).phone(org.getPhone())
                .email(org.getEmail())
                .status(org.getStatus() != null ? org.getStatus().name() : null)
                .description(org.getDescription())
                .memberCount(memberCount != null ? memberCount : 0).children(new ArrayList<>())
                .createdAt(org.getCreatedAt()).updatedAt(org.getUpdatedAt()).build();
    }
}
