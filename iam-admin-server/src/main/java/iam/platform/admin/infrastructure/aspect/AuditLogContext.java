package iam.platform.admin.infrastructure.aspect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import iam.platform.common.model.enums.AuditEventType;
import iam.platform.common.model.enums.AuditResult;

/**
 * Context object holding all information needed for an audit log entry.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogContext {
    private String eventId;
    private String sourceService;
    private Long tenantId;
    private Long userId;
    private String username;
    private AuditEventType eventType;
    private String resourceType;
    private Long resourceId;
    private String action;
    private String ipAddress;
    private String userAgent;
    private String requestUri;
    private String requestParams;
    private AuditResult result;
    private String errorMessage;
    private String traceId;
}
