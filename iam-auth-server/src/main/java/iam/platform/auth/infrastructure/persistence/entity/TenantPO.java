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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_tenant")
@Getter
@NoArgsConstructor
public class TenantPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long id;

    @Setter
    @Column(name = "tenant_code", nullable = false, unique = true, length = 50)
    private String tenantCode;

    @Setter
    @Column(name = "tenant_name", nullable = false, length = 200)
    private String tenantName;

    @Setter
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    @Setter
    @Column(name = "max_users")
    private Integer maxUsers = 100;

    @Setter
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Setter
    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Setter
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @Setter
    private LocalDateTime updatedAt;

    public TenantPO(String tenantCode, String tenantName) {
        this.tenantCode = tenantCode;
        this.tenantName = tenantName;
        this.status = "ACTIVE";
    }
}
