package iam.platform.common.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import iam.platform.common.model.enums.AuditEventType;
import iam.platform.common.model.enums.AuditResult;
import iam.platform.common.model.enums.EventCategory;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogQueryRequest {
    private Long tenantId;
    private Long userId;
    private String username;
    private EventCategory eventCategory;
    private AuditEventType eventType;
    private AuditResult result;
    private String resourceType;
    private Long resourceId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDir = "desc";
}
