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
@Table(name = "t_tenant_account_organization_mapping")
@Getter
@NoArgsConstructor
public class TenantAccountOrganizationMappingPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long id;

    @Setter
    @Column(name = "tenant_account_id", nullable = false)
    private Long tenantAccountId;

    @Setter
    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Setter
    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @Setter
    @Column(length = 100)
    private String position;

    @Setter
    @Column(name = "joined_org_at", nullable = false)
    private LocalDateTime joinedOrgAt;

    public TenantAccountOrganizationMappingPO(Long tenantAccountId, Long organizationId,
            Boolean isPrimary, String position) {
        this.tenantAccountId = tenantAccountId;
        this.organizationId = organizationId;
        this.isPrimary = isPrimary;
        this.position = position;
        this.joinedOrgAt = LocalDateTime.now();
    }
}
