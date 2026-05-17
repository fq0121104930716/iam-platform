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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_organization")
@Getter
@NoArgsConstructor
public class OrganizationPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long id;

    @Setter
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Setter
    @Column(name = "org_code", nullable = false, length = 50)
    private String orgCode;

    @Setter
    @Column(name = "org_name", nullable = false, length = 200)
    private String orgName;

    @Setter
    @Column(name = "org_type", nullable = false, length = 20)
    private String orgType = "DEPARTMENT";

    @Setter
    @Column(name = "parent_id")
    private Long parentId;

    @Setter
    @Column(nullable = false)
    private Integer level = 1;

    @Setter
    @Column(nullable = false, length = 500)
    private String path;

    @Setter
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Setter
    @Column(name = "manager_id")
    private Long managerId;

    @Setter
    @Column(length = 20)
    private String phone;

    @Setter
    @Column(length = 255)
    private String email;

    @Setter
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Setter
    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @Setter
    private LocalDateTime updatedAt;

    public OrganizationPO(Long tenantId, String orgCode, String orgName, String orgType) {
        this.tenantId = tenantId;
        this.orgCode = orgCode;
        this.orgName = orgName;
        this.orgType = orgType;
        this.status = "ACTIVE";
    }
}
