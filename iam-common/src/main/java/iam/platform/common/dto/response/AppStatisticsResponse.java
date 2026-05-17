package iam.platform.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for application statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppStatisticsResponse {

    private Long totalApplications;
    private Long activeApplications;
    private Long inactiveApplications;

    private Map<String, Long> countByType;

    private Long totalAuthorizations;
}
