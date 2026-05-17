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
@Table(name = "t_platform_menu")
@Getter
@NoArgsConstructor
public class PlatformMenuPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long id;

    @Setter
    @Column(name = "menu_code", nullable = false, unique = true, length = 50)
    private String menuCode;

    @Setter
    @Column(name = "menu_name", nullable = false, length = 100)
    private String menuName;

    @Setter
    @Column(length = 50)
    private String icon;

    @Setter
    @Column(length = 100)
    private String path;

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
