package iam.platform.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for tenant-specific dashboard overview statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantOverviewResponse {

    private Long tenantId;
    private String tenantCode;
    private String tenantName;
    private String status;

    private Long userCount;
    private Long activeUserCount;

    private Long applicationCount;
    private Long activeApplicationCount;

    private Long organizationCount;

    private LocalDateTime expiresAt;
}
