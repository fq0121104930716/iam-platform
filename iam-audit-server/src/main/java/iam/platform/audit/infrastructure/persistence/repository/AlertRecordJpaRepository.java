package iam.platform.audit.infrastructure.persistence.repository;

import iam.platform.audit.infrastructure.persistence.entity.AlertRecordPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Alert record JPA repository.
 */
public interface AlertRecordJpaRepository extends JpaRepository<AlertRecordPO, Long> {

    Page<AlertRecordPO> findByRuleId(Long ruleId, Pageable pageable);

    Page<AlertRecordPO> findByStatus(String status, Pageable pageable);

    Page<AlertRecordPO> findByTriggeredAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<AlertRecordPO> findByStatusAndTriggeredAtBefore(String status, LocalDateTime cutoff);
}
