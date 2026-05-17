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
import iam.platform.common.model.enums.PermissionAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_resource_permission")
@Getter
@NoArgsConstructor
public class ResourcePermissionPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "tenant_id")
    private Long tenantId;

    @Setter
    @Column(name = "permission_code", nullable = false, length = 100)
    private String permissionCode;

    @Setter
    @Column(name = "permission_name", nullable = false, length = 200)
    private String permissionName;

    @Setter
    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PermissionAction action;

    @Setter
    @Column(length = 500)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public ResourcePermissionPO(Long tenantId, String permissionCode, String permissionName,
            String resourceType, PermissionAction action, String description) {
        this.tenantId = tenantId;
        this.permissionCode = permissionCode;
        this.permissionName = permissionName;
        this.resourceType = resourceType;
        this.action = action;
        this.description = description;
    }
}
