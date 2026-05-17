package iam.platform.audit.domain.repository;

import iam.platform.audit.domain.model.entity.AlertRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Alert record repository interface (domain layer).
 */
public interface AlertRecordRepository {

    AlertRecord save(AlertRecord alertRecord);

    Optional<AlertRecord> findById(Long id);

    Page<AlertRecord> findByRuleId(Long ruleId, Pageable pageable);

    Page<AlertRecord> findByStatus(String status, Pageable pageable);

    Page<AlertRecord> findByTriggeredAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<AlertRecord> findByStatusAndTriggeredAtBefore(String status, LocalDateTime cutoff);

    Page<AlertRecord> findAll(Pageable pageable);
}
