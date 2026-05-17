package iam.platform.audit.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Alert record domain entity.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRecord {

    private Long id;
    private Long ruleId;
    private LocalDateTime triggeredAt;
    private Integer eventCount;
    private List<String> sampleEventIds;
    private String severity;
    private String status;
    private LocalDateTime notifiedAt;
    private LocalDateTime resolvedAt;
    private String resolvedBy;

    public static AlertRecord create(Long ruleId, Integer eventCount, List<String> sampleEventIds,
                                      String severity) {
        return AlertRecord.builder()
                .ruleId(ruleId)
                .triggeredAt(LocalDateTime.now())
                .eventCount(eventCount)
                .sampleEventIds(sampleEventIds)
                .severity(severity)
                .status("NEW")
                .build();
    }

    public void markNotified() {
        this.notifiedAt = LocalDateTime.now();
    }

    public void markResolved(String resolvedBy) {
        this.status = "RESOLVED";
        this.resolvedAt = LocalDateTime.now();
        this.resolvedBy = resolvedBy;
    }
}
