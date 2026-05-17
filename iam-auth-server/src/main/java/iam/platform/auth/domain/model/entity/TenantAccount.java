package iam.platform.auth.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import iam.platform.common.util.Guard;
import iam.platform.common.model.enums.AccountStatus;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantAccount {
    private Long id;
    private Long userId;
    private Long tenantId;
    private String accountCode;
    private String employeeNo;
    private AccountStatus status;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private String preferredLanguage;
    private String timezone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Lazy loaded roles (will be populated by repository)
    private transient List<Role> roles;

    private static final String DEFAULT_LANGUAGE = "zh-CN";
    private static final String DEFAULT_TIMEZONE = "Asia/Shanghai";

    // ==================== Factory Methods ====================

    /**
     * Create a new TenantAccount with ACTIVE status and default preferences.
     */
    public static TenantAccount create(Long userId, Long tenantId, String accountCode,
            String employeeNo) {
        Guard.notNull(userId, "User ID cannot be null");
        Guard.notNull(tenantId, "Tenant ID cannot be null");
        Guard.notBlank(accountCode, "Account code cannot be blank");

        return TenantAccount.builder().userId(userId).tenantId(tenantId).accountCode(accountCode)
                .employeeNo(employeeNo).status(AccountStatus.ACTIVE).joinedAt(LocalDateTime.now())
                .preferredLanguage(DEFAULT_LANGUAGE).timezone(DEFAULT_TIMEZONE).build();
    }

    // ==================== State Machine ====================

    /**
     * Suspend this account. Only ACTIVE accounts can be suspended.
     */
    public void suspend() {
        Guard.state(status == AccountStatus.ACTIVE, "Only active accounts can be suspended.");
        this.status = AccountStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Reactivate this account. Only SUSPENDED accounts can be reactivated.
     */
    public void reactivate() {
        Guard.state(status == AccountStatus.SUSPENDED,
                "Only suspended accounts can be reactivated.");
        this.status = AccountStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Mark account as LEFT. This is irreversible.
     */
    public void leave() {
        Guard.state(status != AccountStatus.LEFT, "Account has already left.");
        this.status = AccountStatus.LEFT;
        this.leftAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== Behavior Methods ====================

    /**
     * Update account preferences (language and timezone).
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
        return status == AccountStatus.ACTIVE;
    }

    public boolean hasLeft() {
        return status == AccountStatus.LEFT;
    }

    public String getTenantCode() {
        // This will be populated by the repository join query
        return null;
    }
}
