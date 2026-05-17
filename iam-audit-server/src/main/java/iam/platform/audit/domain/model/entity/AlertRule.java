package iam.platform.audit.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Alert rule domain entity.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRule {

    private Long id;
    private String ruleCode;
    private String ruleName;
    private String eventTypeFilter;
    private String conditionExpression;
    private Integer threshold;
    private Integer timeWindowSeconds;
    private String severity;
    private Boolean enabled;
    private String notificationChannels;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

    public static AlertRule create(String ruleCode, String ruleName, String eventTypeFilter,
                                    String conditionExpression, Integer threshold,
                                    Integer timeWindowSeconds, String severity,
                                    String notificationChannels, String createdBy) {
        return AlertRule.builder()
                .ruleCode(ruleCode)
                .ruleName(ruleName)
                .eventTypeFilter(eventTypeFilter)
                .conditionExpression(conditionExpression)
                .threshold(threshold)
                .timeWindowSeconds(timeWindowSeconds)
                .severity(severity)
                .enabled(true)
                .notificationChannels(notificationChannels)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
