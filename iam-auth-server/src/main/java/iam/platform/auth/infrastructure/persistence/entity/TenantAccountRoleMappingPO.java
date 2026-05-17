package iam.platform.auth.infrastructure.persistence.entity;

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
@Table(name = "t_tenant_account_role_mapping")
@Getter
@NoArgsConstructor
public class TenantAccountRoleMappingPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long id;

    @Setter
    @Column(name = "tenant_account_id", nullable = false)
    private Long tenantAccountId;

    @Setter
    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Setter
    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Setter
    @Column(name = "assigned_by", length = 100)
    private String assignedBy;

    public TenantAccountRoleMappingPO(Long tenantAccountId, Long roleId) {
        this.tenantAccountId = tenantAccountId;
        this.roleId = roleId;
        this.assignedAt = LocalDateTime.now();
    }
}
