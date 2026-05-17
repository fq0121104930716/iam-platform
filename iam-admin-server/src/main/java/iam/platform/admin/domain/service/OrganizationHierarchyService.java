package iam.platform.admin.domain.service;

import iam.platform.admin.domain.model.entity.Organization;
import iam.platform.common.model.valueobject.OrganizationPath;

/**
 * Domain service for organization hierarchy operations that require repository
 * access.
 */
public interface OrganizationHierarchyService {

    /**
     * Validate that the target organization is not a descendant of the source.
     * This prevents circular references during reparenting.
     *
     * @param org       the organization being moved
     * @param newParent the proposed new parent
     * @throws iam.platform.common.model.exception.InvalidStateException if circular
     *                                                               reference
     *                                                               detected
     */
    void validateNotDescendant(Organization org, Organization newParent);

    /**
     * Update all children paths after a reparent operation.
     * Replaces the old path prefix with the new one for all descendants.
     *
     * @param tenantId  the tenant the organizations belong to
     * @param oldPrefix the old path prefix to replace
     * @param newPrefix the new path prefix
     */
    void updateChildrenPaths(Long tenantId, OrganizationPath oldPrefix, OrganizationPath newPrefix);
}
