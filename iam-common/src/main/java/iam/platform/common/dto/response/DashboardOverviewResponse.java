package iam.platform.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for global dashboard overview statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewResponse {

    private Long totalTenants;
    private Long activeTenants;

    private Long totalUsers;
    private Long activeUsers;

    private Long totalApplications;
    private Long activeApplications;
}
