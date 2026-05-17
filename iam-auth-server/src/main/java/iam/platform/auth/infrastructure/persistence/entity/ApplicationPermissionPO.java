package iam.platform.auth.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_application_permission")
@Getter
@NoArgsConstructor
public class ApplicationPermissionPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long id;

    @Setter
    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Setter
    @Column(name = "permission_code", nullable = false, length = 100)
    private String permissionCode;

    @Setter
    @Column(name = "permission_name", nullable = false, length = 200)
    private String permissionName;

    @Setter
    @Column(name = "resource_type", nullable = false, length = 100)
    private String resourceType;

    @Setter
    @Column(nullable = false, length = 50)
    private String action;

    @Setter
    @Column(length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @Setter
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
