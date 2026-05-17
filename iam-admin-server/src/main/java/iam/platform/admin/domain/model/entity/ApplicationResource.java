package iam.platform.admin.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import iam.platform.common.util.Guard;
import iam.platform.common.model.enums.ResourceType;

import java.time.LocalDateTime;

/**
 * Application resources (menu, button, API) entity.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResource {
    private Long id;
    private Long applicationId;
    private String resourceCode;
    private String resourceName;
    private ResourceType resourceType;
    private String icon;
    private String path;
    private String apiPath;
    private String apiMethod;
    private Integer sortOrder;
    private Long parentId;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Factory Methods ====================

    /**
     * Create a new ApplicationResource.
     */
    public static ApplicationResource create(Long applicationId, String resourceCode,
            String resourceName, ResourceType resourceType, String icon, String path,
            String apiPath, String apiMethod, Integer sortOrder, Long parentId, String description) {
        Guard.notNull(applicationId, "Application ID cannot be null");
        Guard.notBlank(resourceCode, "Resource code cannot be blank");
        Guard.notBlank(resourceName, "Resource name cannot be blank");
        Guard.notNull(resourceType, "Resource type cannot be null");

        LocalDateTime now = LocalDateTime.now();
        return ApplicationResource.builder()
                .applicationId(applicationId)
                .resourceCode(resourceCode)
                .resourceName(resourceName)
                .resourceType(resourceType)
                .icon(icon)
                .path(path)
                .apiPath(apiPath)
                .apiMethod(apiMethod)
                .sortOrder(sortOrder != null ? sortOrder : 0)
                .parentId(parentId)
                .description(description)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    // ==================== Behavior Methods ====================

    /**
     * Update resource information.
     */
    public void update(String resourceName, String icon, String path, String apiPath,
            String apiMethod, Integer sortOrder, String description) {
        if (resourceName != null) {
            this.resourceName = resourceName;
        }
        if (icon != null) {
            this.icon = icon;
        }
        if (path != null) {
            this.path = path;
        }
        if (apiPath != null) {
            this.apiPath = apiPath;
        }
        if (apiMethod != null) {
            this.apiMethod = apiMethod;
        }
        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }
        if (description != null) {
            this.description = description;
        }
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== Query Methods ====================

    public boolean hasParent() {
        return parentId != null;
    }

    public boolean isRootResource() {
        return parentId == null;
    }

    public boolean isMenu() {
        return resourceType == ResourceType.MENU;
    }

    public boolean isButton() {
        return resourceType == ResourceType.BUTTON;
    }

    public boolean isApi() {
        return resourceType == ResourceType.API;
    }
}
