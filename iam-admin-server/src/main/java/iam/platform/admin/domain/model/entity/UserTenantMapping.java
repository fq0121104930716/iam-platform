package iam.platform.admin.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import iam.platform.common.util.Guard;
import iam.platform.common.model.enums.UserTenantStatus;

import java.time.LocalDateTime;

/**
 * User-Tenant direct mapping entity (replaces TenantAccount).
 * Represents a user's association with a specific tenant.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTenantMapping {
    private Long id;
    private Long userId;
    private Long tenantId;
    private String accountCode;
    private String employeeNo;
    private UserTenantStatus status;
    private String preferredLanguage;
    private String timezone;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Factory Methods ====================

    /**
     * Create a new User-Tenant mapping with ACTIVE status.
     */
    public static UserTenantMapping create(Long userId, Long tenantId, String accountCode,
            String employeeNo, String preferredLanguage, String timezone) {
        Guard.notNull(userId, "User ID cannot be null");
        Guard.notNull(tenantId, "Tenant ID cannot be null");

        LocalDateTime now = LocalDateTime.now();
        return UserTenantMapping.builder()
                .userId(userId)
                .tenantId(tenantId)
                .accountCode(accountCode)
                .employeeNo(employeeNo)
                .status(UserTenantStatus.ACTIVE)
                .preferredLanguage(preferredLanguage != null ? preferredLanguage : "zh-CN")
                .timezone(timezone != null ? timezone : "Asia/Shanghai")
                .joinedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    // ==================== Behavior Methods ====================

    /**
     * Suspend this user-tenant mapping.
     */
    public void suspend() {
        Guard.state(status == UserTenantStatus.ACTIVE,
                "Only active mappings can be suspended.");
        this.status = UserTenantStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Reactivate this user-tenant mapping.
     */
    public void reactivate() {
        Guard.state(status == UserTenantStatus.SUSPENDED,
                "Only suspended mappings can be reactivated.");
        this.status = UserTenantStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Mark that the user has left this tenant.
     */
    public void leave() {
        Guard.state(status != UserTenantStatus.LEFT,
                "User has already left this tenant.");
        this.status = UserTenantStatus.LEFT;
        this.leftAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Update preferences.
     */
    public void updatePreferences(String preferredLanguage, String timezone) {
        if (preferredLanguage != null) {
            this.preferredLanguage = preferredLanguage;
        }
        if (timezone != null) {
            this.timezone = timezone;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Update employee number.
     */
    public void updateEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== Query Methods ====================

    public boolean isActive() {
        return status == UserTenantStatus.ACTIVE;
    }

    public boolean hasLeft() {
        return status == UserTenantStatus.LEFT;
    }
}
