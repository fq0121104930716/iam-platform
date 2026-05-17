package iam.platform.audit.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Alert rule persistence entity (JPA).
 */
@Entity
@Table(name = "t_alert_rule")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRulePO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_code", nullable = false, unique = true, length = 50)
    private String ruleCode;

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Column(name = "event_type_filter", length = 500)
    private String eventTypeFilter;

    @Column(name = "condition_expression", columnDefinition = "TEXT")
    private String conditionExpression;

    private Integer threshold;

    @Column(name = "time_window_seconds")
    private Integer timeWindowSeconds;

    @Column(length = 20)
    private String severity;

    private Boolean enabled;

    @Column(name = "notification_channels", length = 500)
    private String notificationChannels;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (enabled == null) {
            enabled = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
