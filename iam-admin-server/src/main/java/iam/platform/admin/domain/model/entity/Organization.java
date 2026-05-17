package iam.platform.admin.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import iam.platform.common.util.Guard;
import iam.platform.common.model.enums.OrgStatus;
import iam.platform.common.model.enums.OrgType;
import iam.platform.common.model.valueobject.OrganizationPath;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organization {
    private Long id;
    private Long tenantId;
    private String orgCode;
    private String orgName;
    private OrgType orgType;
    private Long parentId;
    private Integer level;
    private String path;
    private Integer sortOrder;
    private Long managerId;
    private String phone;
    private String email;
    private OrgStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Factory Methods ====================

    /**
     * Create a root organization (no parent) for a tenant.
     */
    public static Organization createRoot(Long tenantId, String orgCode, String orgName,
            OrgType orgType, Long managerId, Integer sortOrder,
            String phone, String email, String description) {
        Guard.notNull(tenantId, "Tenant ID cannot be null");
        Guard.notBlank(orgCode, "Organization code cannot be blank");
        Guard.notBlank(orgName, "Organization name cannot be blank");

        OrganizationPath rootPath = OrganizationPath.root(tenantId);

        return Organization.builder()
                .tenantId(tenantId)
                .orgCode(orgCode)
                .orgName(orgName)
                .orgType(orgType)
                .parentId(null)
                .level(0)
                .path(rootPath.getValue())
                .sortOrder(sortOrder != null ? sortOrder : 0)
                .managerId(managerId)
                .phone(phone)
                .email(email)
                .status(OrgStatus.ACTIVE)
                .description(description)
                .build();
    }

    /**
     * Create a child organization under a parent.
     */
    public static Organization createChild(Long tenantId, String orgCode, String orgName,
            OrgType orgType, Organization parent,
            Long managerId, Integer sortOrder,
            String phone, String email, String description) {
        Guard.notNull(tenantId, "Tenant ID cannot be null");
        Guard.notNull(parent, "Parent organization cannot be null");
        Guard.notBlank(orgCode, "Organization code cannot be blank");
        Guard.notBlank(orgName, "Organization name cannot be blank");
        Guard.state(parent.getTenantId().equals(tenantId),
                "Parent organization must belong to the same tenant.");

        return Organization.builder()
                .tenantId(tenantId)
                .orgCode(orgCode)
                .orgName(orgName)
                .orgType(orgType)
                .parentId(parent.getId())
                .level(parent.getLevel() + 1)
                .path(parent.getPath()) // temporary, will be fixed after persist
                .sortOrder(sortOrder != null ? sortOrder : 0)
                .managerId(managerId)
                .phone(phone)
                .email(email)
                .status(OrgStatus.ACTIVE)
                .description(description)
                .build();
    }

    // ==================== Hierarchy Management ====================

    /**
     * Fix the path after persist assigns an ID.
     * Call this immediately after the first save to update the path with the real
     * ID.
     */
    public void fixPathAfterPersist(Long assignedId) {
        Guard.notNull(assignedId, "Assigned ID cannot be null");
        this.id = assignedId;
        if (parentId == null) {
            // Root: path = /{tenantId}/{id}
            this.path = OrganizationPath.root(tenantId).childPath(assignedId).getValue();
        } else {
            // Child: path = parentPath/{id}
            OrganizationPath parentPath = OrganizationPath.of(this.path);
            this.path = parentPath.childPath(assignedId).getValue();
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Reparent this organization under a new parent.
     * Validates that the new parent is in the same tenant.
     */
    public void reparent(Organization newParent) {
        Guard.notNull(newParent, "New parent cannot be null");
        Guard.state(newParent.getTenantId().equals(this.tenantId),
                "New parent must belong to the same tenant.");
        Guard.state(!this.isAncestorOf(newParent),
                "Cannot move organization to its own descendant.");

        this.parentId = newParent.getId();
        this.level = newParent.getLevel() + 1;
        OrganizationPath newParentPath = OrganizationPath.of(newParent.getPath());
        this.path = newParentPath.childPath(this.id).getValue();
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== Status Management ====================

    /**
     * Activate this organization.
     */
    public void activate() {
        this.status = OrgStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Deactivate this organization.
     */
    public void deactivate() {
        this.status = OrgStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== Behavior Methods ====================

    /**
     * Update organization information.
     */
    public void updateInfo(String orgName, OrgType orgType, Long managerId,
            Integer sortOrder, String phone, String email,
            String description) {
        if (orgName != null) {
            this.orgName = orgName;
        }
        if (orgType != null) {
            this.orgType = orgType;
        }
        if (managerId != null) {
            this.managerId = managerId;
        }
        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }
        if (phone != null) {
            this.phone = phone;
        }
        if (email != null) {
            this.email = email;
        }
        if (description != null) {
            this.description = description;
        }
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== Query Methods ====================

    public boolean isRoot() {
        return parentId == null;
    }

    /**
     * Check if this organization is an ancestor of another (based on path).
     */
    public boolean isAncestorOf(Organization other) {
        if (other == null || this.path == null || other.getPath() == null) {
            return false;
        }
        OrganizationPath thisPath = OrganizationPath.of(this.path);
        OrganizationPath otherPath = OrganizationPath.of(other.getPath());
        return thisPath.isAncestorOf(otherPath);
    }

    /**
     * Ensure this organization belongs to the given tenant.
     */
    public void ensureBelongsToTenant(Long tenantId) {
        Guard.state(this.tenantId.equals(tenantId),
                "Organization does not belong to the specified tenant.");
    }

    public OrganizationPath getOrganizationPath() {
        return OrganizationPath.of(this.path);
    }
}
