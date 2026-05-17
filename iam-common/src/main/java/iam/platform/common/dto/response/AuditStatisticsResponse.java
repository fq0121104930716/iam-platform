package iam.platform.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditStatisticsResponse {
    private Long totalLogs;
    private Map<String, Long> byCategory;
    private Map<String, Long> byResult;
    private List<TopEventType> topEventTypes;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopEventType {
        private String eventType;
        private Long count;
    }
}
