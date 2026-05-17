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
@Table(name = "t_application_resource")
@Getter
@NoArgsConstructor
public class ApplicationResourcePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long id;

    @Setter
    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Setter
    @Column(name = "resource_code", nullable = false, length = 100)
    private String resourceCode;

    @Setter
    @Column(name = "resource_name", nullable = false, length = 100)
    private String resourceName;

    @Setter
    @Column(name = "resource_type", nullable = false, length = 20)
    private String resourceType;

    @Setter
    @Column(length = 50)
    private String icon;

    @Setter
    @Column(length = 200)
    private String path;

    @Setter
    @Column(name = "api_path", length = 200)
    private String apiPath;

    @Setter
    @Column(name = "api_method", length = 10)
    private String apiMethod;

    @Setter
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Setter
    @Column(name = "parent_id")
    private Long parentId;

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
}
