package iam.platform.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for person statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonStatisticsResponse {

    private Long totalPersons;
    private Long activePersons;

    private Long newPersonsToday;
    private Long newPersonsThisWeek;
    private Long newPersonsThisMonth;

    private Double loginSuccessRate;

    private Long totalLoginsToday;
    private Long failedLoginsToday;
}
