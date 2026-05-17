package iam.platform.audit.domain.repository;

import iam.platform.audit.domain.model.entity.AlertRule;

import java.util.List;
import java.util.Optional;

/**
 * Alert rule repository interface (domain layer).
 */
public interface AlertRuleRepository {

    AlertRule save(AlertRule alertRule);

    Optional<AlertRule> findById(Long id);

    Optional<AlertRule> findByRuleCode(String ruleCode);

    List<AlertRule> findAllEnabled();

    List<AlertRule> findAll();

    void deleteById(Long id);
}
