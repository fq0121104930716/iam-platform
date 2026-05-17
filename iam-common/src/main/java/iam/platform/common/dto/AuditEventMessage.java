package iam.platform.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Audit event message for RocketMQ transmission.
 * This DTO is the contract between service producers and the audit consumer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditEventMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique event identifier for deduplication */
    private String eventId;

    /** ISO 8601 timestamp when event occurred */
    private String timestamp;

    /** Source service name (e.g., "iam-auth-service", "iam-admin-service") */
    private String sourceService;

    /** Tenant identifier */
    private Long tenantId;

    /** Person/user identifier */
    private Long personId;

    /** Username of the actor */
    private String username;

    /** Event type name from AuditEventType enum */
    private String eventType;

    /** Event category name from EventCategory enum */
    private String eventCategory;

    /** Resource type (e.g., "user", "tenant", "application") */
    private String resourceType;

    /** Resource identifier */
    private Long resourceId;

    /** Human-readable action description */
    private String action;

    /** Client IP address */
    private String ipAddress;

    /** User-Agent header */
    private String userAgent;

    /** Request URI */
    private String requestUri;

    /** Request parameters as JSON (may be masked for sensitive fields) */
    private String requestParams;

    /** Result: SUCCESS or FAILURE */
    private String result;

    /** Error message if result is FAILURE */
    private String errorMessage;

    /** Distributed tracing correlation ID */
    private String traceId;
}
