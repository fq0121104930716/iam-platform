package iam.platform.admin.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import iam.platform.common.util.Guard;

import java.time.LocalDateTime;

/**
 * Application-Tenant mapping entity.
 * Controls which applications are available to which tenants.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationTenantMapping {
    private Long id;
    private Long applicationId;
    private Long tenantId;
    private Boolean enabled;
    private LocalDateTime createdAt;

    // ==================== Factory Methods ====================

    /**
     * Create a new Application-Tenant mapping.
     */
    public static ApplicationTenantMapping create(Long applicationId, Long tenantId, Boolean enabled) {
        Guard.notNull(applicationId, "Application ID cannot be null");
        Guard.notNull(tenantId, "Tenant ID cannot be null");

        return ApplicationTenantMapping.builder()
                .applicationId(applicationId)
                .tenantId(tenantId)
                .enabled(enabled != null ? enabled : true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ==================== Behavior Methods ====================

    /**
     * Enable this application for the tenant.
     */
    public void enable() {
        this.enabled = true;
    }

    /**
     * Disable this application for the tenant.
     */
    public void disable() {
        this.enabled = false;
    }

    // ==================== Query Methods ====================

    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }
}
