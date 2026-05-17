package iam.platform.audit.infrastructure.persistence.repository;

import iam.platform.audit.infrastructure.persistence.entity.AlertRulePO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Alert rule JPA repository.
 */
public interface AlertRuleJpaRepository extends JpaRepository<AlertRulePO, Long> {

    Optional<AlertRulePO> findByRuleCode(String ruleCode);

    List<AlertRulePO> findByEnabledTrue();
}
