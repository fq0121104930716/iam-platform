package iam.platform.auth.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import iam.platform.common.util.Guard;
import iam.platform.common.model.enums.PermissionAction;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourcePermission {
    private Long id;
    private Long tenantId; // 权限归属租户，null表示全局权限
    private String permissionCode; // 如 "user:read", "order:write"
    private String permissionName;
    private String resourceType; // 如 "user", "application", "report"
    private PermissionAction action; // READ/WRITE/DELETE/EXPORT/APPROVE
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Factory Methods ====================

    /**
     * Create a resource permission. Permission code is auto-generated as
     * "resourceType:ACTION".
     */
    public static ResourcePermission create(Long tenantId, String resourceType,
            PermissionAction action, String name, String description) {
        Guard.notBlank(resourceType, "Resource type cannot be blank");
        Guard.notNull(action, "Action cannot be null");
        Guard.notBlank(name, "Permission name cannot be blank");

        String permissionCode = resourceType + ":" + action.name().toLowerCase();

        return ResourcePermission.builder()
                .tenantId(tenantId)
                .permissionCode(permissionCode)
                .permissionName(name)
                .resourceType(resourceType)
                .action(action)
                .description(description)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== Behavior Methods ====================

    public void updateInfo(String name, String description) {
        if (name != null) {
            this.permissionName = name;
        }
        if (description != null) {
            this.description = description;
        }
        this.updatedAt = LocalDateTime.now();
    }

    public boolean matchesResourceType(String type) {
        return this.resourceType != null && this.resourceType.equals(type);
    }

    public boolean belongsToTenant(Long tenantId) {
        return this.tenantId != null && this.tenantId.equals(tenantId);
    }

    public boolean isGlobalPermission() {
        return this.tenantId == null;
    }
}
