package iam.platform.audit.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Alert record persistence entity (JPA).
 */
@Entity
@Table(name = "t_alert_record", indexes = {
    @Index(name = "idx_alert_rule_id", columnList = "rule_id"),
    @Index(name = "idx_alert_status", columnList = "status"),
    @Index(name = "idx_alert_triggered_at", columnList = "triggered_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRecordPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt;

    @Column(name = "event_count")
    private Integer eventCount;

    @Column(name = "sample_event_ids", columnDefinition = "TEXT")
    private String sampleEventIds;

    @Column(length = 20)
    private String severity;

    @Column(length = 20)
    private String status;

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by", length = 100)
    private String resolvedBy;

    @PrePersist
    public void prePersist() {
        if (triggeredAt == null) {
            triggeredAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "NEW";
        }
    }
}
