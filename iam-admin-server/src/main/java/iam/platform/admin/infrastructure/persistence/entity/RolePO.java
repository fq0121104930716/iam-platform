package iam.platform.admin.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import iam.platform.common.model.enums.RoleType;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_role")
@Getter
@NoArgsConstructor
public class RolePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "tenant_id")
    private Long tenantId;

    @Setter
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Setter
    @Column(nullable = false, length = 100)
    private String name;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, length = 20)
    private RoleType roleType;

    @Setter
    @Column(length = 500)
    private String description;

    @Setter
    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public RolePO(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.roleType = RoleType.SYSTEM;
        this.isSystem = false;
    }

    public RolePO(Long tenantId, String code, String name, RoleType roleType, String description,
            Boolean isSystem) {
        this.tenantId = tenantId;
        this.code = code;
        this.name = name;
        this.roleType = roleType;
        this.description = description;
        this.isSystem = isSystem;
    }
}
