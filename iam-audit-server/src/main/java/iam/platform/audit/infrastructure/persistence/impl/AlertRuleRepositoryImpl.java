package iam.platform.audit.infrastructure.persistence.impl;

import iam.platform.audit.domain.model.entity.AlertRule;
import iam.platform.audit.domain.repository.AlertRuleRepository;
import iam.platform.audit.infrastructure.persistence.entity.AlertRulePO;
import iam.platform.audit.infrastructure.persistence.repository.AlertRuleJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Alert rule repository implementation.
 */
@Repository
@RequiredArgsConstructor
public class AlertRuleRepositoryImpl implements AlertRuleRepository {

    private final AlertRuleJpaRepository jpaRepository;

    @Override
    public AlertRule save(AlertRule alertRule) {
        AlertRulePO po = AlertRulePO.builder()
                .id(alertRule.getId())
                .ruleCode(alertRule.getRuleCode())
                .ruleName(alertRule.getRuleName())
                .eventTypeFilter(alertRule.getEventTypeFilter())
                .conditionExpression(alertRule.getConditionExpression())
                .threshold(alertRule.getThreshold())
                .timeWindowSeconds(alertRule.getTimeWindowSeconds())
                .severity(alertRule.getSeverity())
                .enabled(alertRule.getEnabled())
                .notificationChannels(alertRule.getNotificationChannels())
                .createdAt(alertRule.getCreatedAt())
                .updatedAt(alertRule.getUpdatedAt())
                .createdBy(alertRule.getCreatedBy())
                .build();
        
        AlertRulePO savedPo = jpaRepository.save(po);
        
        return AlertRule.builder()
                .id(savedPo.getId())
                .ruleCode(savedPo.getRuleCode())
                .ruleName(savedPo.getRuleName())
                .eventTypeFilter(savedPo.getEventTypeFilter())
                .conditionExpression(savedPo.getConditionExpression())
                .threshold(savedPo.getThreshold())
                .timeWindowSeconds(savedPo.getTimeWindowSeconds())
                .severity(savedPo.getSeverity())
                .enabled(savedPo.getEnabled())
                .notificationChannels(savedPo.getNotificationChannels())
                .createdAt(savedPo.getCreatedAt())
                .updatedAt(savedPo.getUpdatedAt())
                .createdBy(savedPo.getCreatedBy())
                .build();
    }

    @Override
    public Optional<AlertRule> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<AlertRule> findByRuleCode(String ruleCode) {
        return jpaRepository.findByRuleCode(ruleCode).map(this::toDomain);
    }

    @Override
    public List<AlertRule> findAllEnabled() {
        return jpaRepository.findByEnabledTrue().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<AlertRule> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private AlertRule toDomain(AlertRulePO po) {
        return AlertRule.builder()
                .id(po.getId())
                .ruleCode(po.getRuleCode())
                .ruleName(po.getRuleName())
                .eventTypeFilter(po.getEventTypeFilter())
                .conditionExpression(po.getConditionExpression())
                .threshold(po.getThreshold())
                .timeWindowSeconds(po.getTimeWindowSeconds())
                .severity(po.getSeverity())
                .enabled(po.getEnabled())
                .notificationChannels(po.getNotificationChannels())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .createdBy(po.getCreatedBy())
                .build();
    }
}
