package iam.platform.auth.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import iam.platform.common.util.Guard;
import iam.platform.common.model.enums.TenantStatus;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {
    private Long id;
    private String tenantCode;
    private String tenantName;
    private TenantStatus status;
    private Integer maxUsers;
    private LocalDateTime expiresAt;
    private String contactEmail;
    private String contactPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Factory Methods ====================

    /**
     * Create a new Tenant with ACTIVE status.
     */
    public static Tenant create(String tenantCode, String tenantName, Integer maxUsers,
            LocalDateTime expiresAt, String contactEmail,
            String contactPhone) {
        Guard.notBlank(tenantCode, "Tenant code cannot be blank");
        Guard.notBlank(tenantName, "Tenant name cannot be blank");

        return Tenant.builder()
                .tenantCode(tenantCode)
                .tenantName(tenantName)
                .status(TenantStatus.ACTIVE)
                .maxUsers(maxUsers != null ? maxUsers : 100)
                .expiresAt(expiresAt)
                .contactEmail(contactEmail)
                .contactPhone(contactPhone)
                .build();
    }

    // ==================== State Machine ====================

    /**
     * Activate the tenant. Cannot activate a DELETED tenant.
     */
    public void activate() {
        Guard.state(status != TenantStatus.DELETED,
                "Cannot activate a deleted tenant.");
        this.status = TenantStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Suspend the tenant. Only ACTIVE tenants can be suspended.
     */
    public void suspend() {
        Guard.state(status == TenantStatus.ACTIVE,
                "Only active tenants can be suspended.");
        this.status = TenantStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Mark tenant as deleted. Cannot delete an ACTIVE tenant (suspend first).
     */
    public void markDeleted() {
        Guard.state(status != TenantStatus.ACTIVE,
                "Cannot delete an active tenant. Please suspend it first.");
        this.status = TenantStatus.DELETED;
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== Behavior Methods ====================

    /**
     * Update tenant information.
     */
    public void updateInfo(String tenantName, Integer maxUsers, String contactEmail,
            String contactPhone, LocalDateTime expiresAt) {
        if (tenantName != null) {
            this.tenantName = tenantName;
        }
        if (maxUsers != null) {
            this.maxUsers = maxUsers;
        }
        if (contactEmail != null) {
            this.contactEmail = contactEmail;
        }
        if (contactPhone != null) {
            this.contactPhone = contactPhone;
        }
        if (expiresAt != null) {
            this.expiresAt = expiresAt;
        }
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== Query Methods ====================

    public boolean isActive() {
        return status == TenantStatus.ACTIVE;
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
}
