package iam.platform.admin.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import iam.platform.common.util.Guard;
import iam.platform.common.model.enums.RoleType;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    private Long id;
    private Long tenantId; // 角色归属租户，null表示全局角色
    private String code;
    private String name;
    private RoleType roleType; // SYSTEM/TENANT_CUSTOM
    private String description;
    private Boolean isSystem; // 系统内置角色不可删除
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Factory Methods ====================

    /**
     * Create a role with full parameters.
     */
    public static Role create(Long tenantId, String code, String name, RoleType roleType,
            String description, Boolean isSystem) {
        Guard.notBlank(code, "Role code cannot be blank");
        Guard.notBlank(name, "Role name cannot be blank");
        Guard.notNull(roleType, "Role type cannot be null");

        return Role.builder()
                .tenantId(tenantId)
                .code(code)
                .name(name)
                .roleType(roleType)
                .description(description)
                .isSystem(isSystem != null ? isSystem : false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create a system role (global, immutable).
     */
    public static Role createSystem(String code, String name, String description) {
        return create(null, code, name, RoleType.SYSTEM, description, true);
    }

    /**
     * Create a tenant-custom role.
     */
    public static Role createTenant(Long tenantId, String code, String name, String description) {
        Guard.notNull(tenantId, "Tenant ID cannot be null for tenant role");
        return create(tenantId, code, name, RoleType.TENANT_CUSTOM, description, false);
    }

    // ==================== Behavior Methods ====================

    public boolean canBeDeleted() {
        return !Boolean.TRUE.equals(this.isSystem);
    }

    public boolean isSystemRole() {
        return Boolean.TRUE.equals(this.isSystem);
    }

    public boolean isGlobalRole() {
        return this.tenantId == null;
    }

    public boolean belongsToTenant(Long tenantId) {
        return this.tenantId != null && this.tenantId.equals(tenantId);
    }
}
