package iam.platform.audit.domain.model.entity;

import iam.platform.common.model.enums.AuditEventType;
import iam.platform.common.model.enums.AuditResult;
import iam.platform.common.model.enums.EventCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Audit log domain entity.
 * Immutable entity representing a single audit event.
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
    private List<String> encryptedFields;
    private LocalDateTime createdAt;

    /**
     * Factory method to create AuditLog from event message
     */
    public static AuditLog fromEventMessage(
            String eventId,
            String sourceService,
            Long tenantId,
            Long userId,
            String username,
            AuditEventType eventType,
            EventCategory eventCategory,
            Long resourceId,
            String resourceType,
            String action,
            String ipAddress,
            String userAgent,
            String requestUri,
            String requestParams,
            AuditResult result,
            String errorMessage,
            String traceId) {
        
        return AuditLog.builder()
                .eventId(eventId)
                .sourceService(sourceService)
                .tenantId(tenantId)
                .userId(userId)
                .username(username)
                .eventType(eventType)
                .eventCategory(eventCategory)
                .resourceId(resourceId)
                .resourceType(resourceType)
                .action(action)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .requestUri(requestUri)
                .requestParams(requestParams)
                .result(result)
                .errorMessage(errorMessage)
                .traceId(traceId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public boolean isSuccess() {
        return result == AuditResult.SUCCESS;
    }

    public boolean isFailure() {
        return result == AuditResult.FAILURE;
    }

    public long getAgeInDays() {
        if (createdAt == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
    }
}
