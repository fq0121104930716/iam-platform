package iam.platform.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for user statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsResponse {

    private Long totalUsers;
    private Long activeUsers;

    private Long newUsersToday;
    private Long newUsersThisWeek;
    private Long newUsersThisMonth;

    private Double loginSuccessRate;

    private Long totalLoginsToday;
    private Long failedLoginsToday;
}
