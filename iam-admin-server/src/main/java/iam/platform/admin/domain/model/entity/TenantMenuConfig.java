package iam.platform.admin.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import iam.platform.common.util.Guard;

import java.time.LocalDateTime;

/**
 * Tenant menu configuration entity.
 * Controls which platform menus are visible to a specific tenant.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantMenuConfig {
    private Long id;
    private Long tenantId;
    private Long menuId;
    private Boolean enabled;
    private LocalDateTime createdAt;

    // ==================== Factory Methods ====================

    /**
     * Create a new TenantMenuConfig.
     */
    public static TenantMenuConfig create(Long tenantId, Long menuId, Boolean enabled) {
        Guard.notNull(tenantId, "Tenant ID cannot be null");
        Guard.notNull(menuId, "Menu ID cannot be null");

        return TenantMenuConfig.builder()
                .tenantId(tenantId)
                .menuId(menuId)
                .enabled(enabled != null ? enabled : true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ==================== Behavior Methods ====================

    /**
     * Enable this menu for the tenant.
     */
    public void enable() {
        this.enabled = true;
    }

    /**
     * Disable this menu for the tenant.
     */
    public void disable() {
        this.enabled = false;
    }

    // ==================== Query Methods ====================

    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }
}
