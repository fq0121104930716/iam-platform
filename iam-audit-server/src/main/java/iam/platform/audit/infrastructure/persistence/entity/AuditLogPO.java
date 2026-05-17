package iam.platform.audit.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Audit log persistence entity (JPA).
 */
@Entity
@Table(name = "t_audit_log", indexes = {
    @Index(name = "idx_audit_tenant_id", columnList = "tenant_id"),
    @Index(name = "idx_audit_person_id", columnList = "person_id"),
    @Index(name = "idx_audit_event_type", columnList = "event_type"),
    @Index(name = "idx_audit_event_category", columnList = "event_category"),
    @Index(name = "idx_audit_resource", columnList = "resource_type, resource_id"),
    @Index(name = "idx_audit_result", columnList = "result"),
    @Index(name = "idx_audit_created_at", columnList = "created_at"),
    @Index(name = "idx_audit_tenant_category_time", columnList = "tenant_id, event_category, created_at"),
    @Index(name = "idx_audit_source_service", columnList = "source_service"),
    @Index(name = "idx_audit_event_id", columnList = "event_id"),
    @Index(name = "idx_audit_trace_id", columnList = "trace_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private String eventId;

    @Column(name = "source_service", nullable = false, length = 50)
    private String sourceService;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "person_id")
    private Long userId;

    @Column(length = 100)
    private String username;

    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    @Column(name = "event_category", nullable = false, length = 20)
    private String eventCategory;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "resource_type", length = 50)
    private String resourceType;

    @Column(length = 200)
    private String action;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "request_uri", length = 500)
    private String requestUri;

    @Column(name = "request_params", columnDefinition = "TEXT")
    private String requestParams;

    @Column(nullable = false, length = 10)
    private String result;

    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(name = "encrypted_fields", length = 200)
    private String encryptedFields;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
