package iam.platform.admin.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import iam.platform.common.util.Guard;

import java.time.LocalDateTime;

/**
 * User-Role mapping entity with tenant context.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleMapping {
    private Long id;
    private Long userId;
    private Long tenantId;
    private Long roleId;
    private LocalDateTime assignedAt;
    private Long assignedBy;

    // ==================== Factory Methods ====================

    /**
     * Create a new User-Role mapping.
     */
    public static UserRoleMapping create(Long userId, Long tenantId, Long roleId, Long assignedBy) {
        Guard.notNull(userId, "User ID cannot be null");
        Guard.notNull(tenantId, "Tenant ID cannot be null");
        Guard.notNull(roleId, "Role ID cannot be null");

        return UserRoleMapping.builder()
                .userId(userId)
                .tenantId(tenantId)
                .roleId(roleId)
                .assignedBy(assignedBy)
                .assignedAt(LocalDateTime.now())
                .build();
    }

    // ==================== Query Methods ====================

    public boolean isAssignedByAdmin() {
        return assignedBy != null;
    }
}
