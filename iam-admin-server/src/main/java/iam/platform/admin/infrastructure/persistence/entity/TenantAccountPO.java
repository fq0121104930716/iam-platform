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
@Table(name = "t_tenant_account")
@Getter
@NoArgsConstructor
public class TenantAccountPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long id;

    @Setter
    @Column(name = "person_id", nullable = false)
    private Long personId;

    @Setter
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Setter
    @Column(name = "account_code", nullable = false, length = 50)
    private String accountCode;

    @Setter
    @Column(name = "employee_no", length = 50)
    private String employeeNo;

    @Setter
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Setter
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Setter
    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Setter
    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage = "zh-CN";

    @Setter
    @Column(length = 50)
    private String timezone = "Asia/Shanghai";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @Setter
    private LocalDateTime updatedAt;

    public TenantAccountPO(Long personId, Long tenantId, String accountCode) {
        this.personId = personId;
        this.tenantId = tenantId;
        this.accountCode = accountCode;
        this.status = "ACTIVE";
        this.joinedAt = LocalDateTime.now();
    }
}
