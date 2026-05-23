package iam.platform.admin.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import iam.platform.common.util.Guard;
import iam.platform.common.model.enums.AuditEventType;
import iam.platform.common.model.enums.AuditResult;
import iam.platform.common.model.enums.EventCategory;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Audit log domain entity. Represents an immutable audit event. Once created, audit logs should
 * never be modified.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    private Long id;
    private String eventId;
    private String sourceService;
    private Long tenantId;
    private Long userId;
    private String username;
    private AuditEventType eventType;
    private EventCategory eventCategory;
    private Long resourceId;
    private String resourceType;
    private String action;
    private String ipAddress;
    private String userAgent;
    private String requestUri;
    private String requestParams;
    private AuditResult result;
    private String errorMessage;
    private String traceId;
    private String spanId;
    private String parentSpanId;
    private String encryptedFields;
    private LocalDateTime createdAt;

    // ==================== Factory Methods ====================

    /**
     * Create an audit log from context (used by AOP aspect).
     */
    public static AuditLog fromContext(
            iam.platform.admin.infrastructure.aspect.AuditLogContext context) {
        Guard.notNull(context.getEventType(), "Event type cannot be null");
        Guard.notNull(context.getResult(), "Audit result cannot be null");

        LocalDateTime now = LocalDateTime.now();
        EventCategory category = context.getEventType().getCategory();

        return AuditLog.builder().eventId(context.getEventId())
                .sourceService(context.getSourceService()).tenantId(context.getTenantId())
                .userId(context.getUserId()).username(context.getUsername())
                .eventType(context.getEventType()).eventCategory(category)
                .resourceId(context.getResourceId()).resourceType(context.getResourceType())
                .action(context.getAction()).ipAddress(context.getIpAddress())
                .userAgent(context.getUserAgent()).requestUri(context.getRequestUri())
                .requestParams(context.getRequestParams()).result(context.getResult())
                .errorMessage(context.getErrorMessage()).traceId(context.getTraceId())
                .spanId(context.getSpanId()).parentSpanId(context.getParentSpanId())
                .encryptedFields(context.getEncryptedFields())
                .createdAt(now).build();
    }

    /**
     * Create an audit log directly (for manual logging).
     */
    public static AuditLog create(String eventId, String sourceService, Long tenantId, Long userId,
            String username, AuditEventType eventType, EventCategory eventCategory, Long resourceId,
            String resourceType, String action, String ipAddress, String userAgent,
            String requestUri, String requestParams, AuditResult result, String errorMessage,
            String traceId) {
        Guard.notNull(eventType, "Event type cannot be null");
        Guard.notNull(result, "Audit result cannot be null");

        return AuditLog.builder().eventId(eventId).sourceService(sourceService).tenantId(tenantId)
                .userId(userId).username(username).eventType(eventType).eventCategory(eventCategory)
                .resourceId(resourceId).resourceType(resourceType).action(action)
                .ipAddress(ipAddress).userAgent(userAgent).requestUri(requestUri)
                .requestParams(requestParams).result(result).errorMessage(errorMessage)
                .traceId(traceId).createdAt(LocalDateTime.now()).build();
    }

    // ==================== Query Methods ====================

    public boolean isSuccess() {
        return result == AuditResult.SUCCESS;
    }

    public long getAgeInDays() {
        if (createdAt == null) {
            return 0;
        }
        return Duration.between(createdAt, LocalDateTime.now()).toDays();
    }

    public String getEventCategoryName() {
        return eventCategory != null ? eventCategory.name() : "UNKNOWN";
    }
}
