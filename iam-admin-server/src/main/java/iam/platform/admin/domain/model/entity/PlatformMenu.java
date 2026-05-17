package iam.platform.admin.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import iam.platform.common.util.Guard;

import java.time.LocalDateTime;

/**
 * Platform function menu definitions.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformMenu {
    private Long id;
    private String menuCode;
    private String menuName;
    private String icon;
    private String path;
    private Integer sortOrder;
    private Long parentId;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Factory Methods ====================

    /**
     * Create a new PlatformMenu.
     */
    public static PlatformMenu create(String menuCode, String menuName, String icon,
            String path, Integer sortOrder, Long parentId, String description) {
        Guard.notBlank(menuCode, "Menu code cannot be blank");
        Guard.notBlank(menuName, "Menu name cannot be blank");

        LocalDateTime now = LocalDateTime.now();
        return PlatformMenu.builder()
                .menuCode(menuCode)
                .menuName(menuName)
                .icon(icon)
                .path(path)
                .sortOrder(sortOrder != null ? sortOrder : 0)
                .parentId(parentId)
                .description(description)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    // ==================== Behavior Methods ====================

    /**
     * Update menu information.
     */
    public void update(String menuName, String icon, String path,
            Integer sortOrder, String description) {
        if (menuName != null) {
            this.menuName = menuName;
        }
        if (icon != null) {
            this.icon = icon;
        }
        if (path != null) {
            this.path = path;
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

    public boolean isRootMenu() {
        return parentId == null;
    }
}
