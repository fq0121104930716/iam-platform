package iam.platform.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private String eventId;
    private String sourceService;
    private Long tenantId;
    private Long personId;
    private String username;
    private String eventType;
    private String eventCategory;
    private Long resourceId;
    private String resourceType;
    private String action;
    private String ipAddress;
    private String userAgent;
    private String requestUri;
    private String result;
    private String errorMessage;
    private String traceId;
    private LocalDateTime createdAt;
}
