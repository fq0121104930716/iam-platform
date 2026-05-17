package iam.platform.admin.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import iam.platform.admin.domain.model.entity.Organization;
import iam.platform.common.model.exception.InvalidStateException;
import iam.platform.common.model.valueobject.OrganizationPath;
import iam.platform.admin.domain.repository.OrganizationRepository;
import iam.platform.admin.domain.service.OrganizationHierarchyService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationHierarchyServiceImpl implements OrganizationHierarchyService {

    private final OrganizationRepository organizationRepository;

    @Override
    public void validateNotDescendant(Organization org, Organization newParent) {
        if (org.isAncestorOf(newParent)) {
            throw new InvalidStateException(
                    "Cannot move organization to its own descendant.");
        }

        // Also check via path prefix from database for safety
        String orgPath = org.getPath();
        if (orgPath != null && newParent.getPath() != null
                && newParent.getPath().startsWith(orgPath + "/")) {
            throw new InvalidStateException(
                    "Cannot move organization to its own descendant.");
        }
    }

    @Override
    public void updateChildrenPaths(Long tenantId, OrganizationPath oldPrefix,
            OrganizationPath newPrefix) {
        // Find all organizations whose path starts with the old prefix
        List<Organization> descendants = organizationRepository
                .findByPathStartingWith(oldPrefix.getValue());

        for (Organization child : descendants) {
            String childPath = child.getPath();
            if (childPath != null && childPath.startsWith(oldPrefix.getValue())) {
                String newPath = newPrefix.getValue()
                        + childPath.substring(oldPrefix.getValue().length());
                // Use builder to create updated version and save
                Organization updated = Organization.builder()
                        .id(child.getId())
                        .tenantId(child.getTenantId())
                        .orgCode(child.getOrgCode())
                        .orgName(child.getOrgName())
                        .orgType(child.getOrgType())
                        .parentId(child.getParentId())
                        .level(newPrefix.calculateLevel()
                                + OrganizationPath.of(childPath).calculateLevel()
                                - oldPrefix.calculateLevel())
                        .path(newPath)
                        .sortOrder(child.getSortOrder())
                        .managerId(child.getManagerId())
                        .phone(child.getPhone())
                        .email(child.getEmail())
                        .status(child.getStatus())
                        .description(child.getDescription())
                        .createdAt(child.getCreatedAt())
                        .updatedAt(child.getUpdatedAt())
                        .build();
                organizationRepository.save(updated);
            }
        }
    }
}
