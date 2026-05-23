package iam.platform.admin.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_user_role_mapping")
@Getter
@NoArgsConstructor
public class TenantAccountRoleMappingPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long id;

    @Setter
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Setter
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Setter
    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Setter
    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Setter
    @Column(name = "assigned_by")
    private Long assignedBy;

    public TenantAccountRoleMappingPO(Long userId, Long tenantId, Long roleId) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.roleId = roleId;
        this.assignedAt = LocalDateTime.now();
    }
}
