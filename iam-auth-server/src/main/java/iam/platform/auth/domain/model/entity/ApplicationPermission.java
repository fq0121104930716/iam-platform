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
public class ApplicationPermission {
    private Long id;
    private Long applicationId; // 归属应用
    private String permissionCode; // 权限编码（如 "app:user:read"）
    private String permissionName;
    private String resourceType; // 资源类型（如 "user", "order", "report"）
    private PermissionAction action; // 操作（READ/WRITE/DELETE/EXECUTE）
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Factory Methods ====================

    /**
     * Create an application-level permission. Permission code is auto-generated as
     * "app-{applicationId}:{resourceType:action}".
     */
    public static ApplicationPermission create(Long applicationId, String resourceType,
            PermissionAction action, String name, String description) {
        Guard.notNull(applicationId, "Application ID cannot be null");
        Guard.positive(applicationId.intValue(), "Application ID must be positive");
        Guard.notBlank(resourceType, "Resource type cannot be blank");
        Guard.notNull(action, "Action cannot be null");
        Guard.notBlank(name, "Permission name cannot be blank");

        String permissionCode = "app:" + resourceType + ":" + action.name().toLowerCase();

        return ApplicationPermission.builder()
                .applicationId(applicationId)
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

    public boolean belongsToApplication(Long applicationId) {
        return this.applicationId != null && this.applicationId.equals(applicationId);
    }
}
