package iam.platform.admin.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import iam.platform.common.model.enums.AuditEventType;
import iam.platform.common.model.enums.AuditResult;
import iam.platform.common.model.enums.EventCategory;

import java.time.LocalDateTime;

/**
 * JPA entity for audit log.
 */
@Entity
@Table(name = "t_audit_log", indexes = {
                @Index(name = "idx_audit_tenant_id", columnList = "tenant_id"),
                @Index(name = "idx_audit_user_id", columnList = "user_id"),
                @Index(name = "idx_audit_event_type", columnList = "event_type"),
                @Index(name = "idx_audit_event_category", columnList = "event_category"),
                @Index(name = "idx_audit_resource", columnList = "resource_type, resource_id"),
                @Index(name = "idx_audit_result", columnList = "result"),
                @Index(name = "idx_audit_created_at", columnList = "created_at"),
                @Index(name = "idx_audit_tenant_category_time",
                                columnList = "tenant_id, event_category, created_at"),
                @Index(name = "idx_audit_trace_id", columnList = "trace_id"),
                @Index(name = "idx_audit_event_id", columnList = "event_id"),
                @Index(name = "idx_audit_source_service", columnList = "source_service")})
@Getter
@Setter
@NoArgsConstructor
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

        @Column(name = "user_id")
        private Long userId;

        @Column(name = "username", length = 100)
        private String username;

        @Enumerated(EnumType.STRING)
        @Column(name = "event_type", nullable = false, length = 30)
        private AuditEventType eventType;

        @Enumerated(EnumType.STRING)
        @Column(name = "event_category", nullable = false, length = 20)
        private EventCategory eventCategory;

        @Column(name = "resource_id")
        private Long resourceId;

        @Column(name = "resource_type", length = 50)
        private String resourceType;

        @Column(name = "action", length = 200)
        private String action;

        @Column(name = "ip_address", length = 45)
        private String ipAddress;

        @Column(name = "user_agent", length = 500)
        private String userAgent;

        @Column(name = "request_uri", length = 500)
        private String requestUri;

        @Column(name = "request_params", columnDefinition = "TEXT")
        private String requestParams;

        @Enumerated(EnumType.STRING)
        @Column(name = "result", nullable = false, length = 10)
        private AuditResult result;

        @Column(name = "error_message", length = 2000)
        private String errorMessage;

        @Column(name = "trace_id", length = 100)
        private String traceId;

        @Column(name = "span_id", length = 100)
        private String spanId;

        @Column(name = "parent_span_id", length = 100)
        private String parentSpanId;

        @Column(name = "encrypted_fields", length = 200)
        private String encryptedFields;

        @CreationTimestamp
        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;
}
