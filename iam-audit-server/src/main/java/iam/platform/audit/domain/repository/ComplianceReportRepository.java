package iam.platform.audit.domain.repository;

import iam.platform.audit.domain.model.entity.ComplianceReport;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Compliance report repository interface (domain layer).
 */
public interface ComplianceReportRepository {

    ComplianceReport save(ComplianceReport report);

    Optional<ComplianceReport> findById(Long id);

    Optional<ComplianceReport> findByReportCode(String reportCode);

    Page<ComplianceReport> findByReportType(String reportType, Pageable pageable);

    Page<ComplianceReport> findByStatus(String status, Pageable pageable);

    Page<ComplianceReport> findByPeriodBetween(LocalDateTime start, LocalDateTime end,
            Pageable pageable);

    Page<ComplianceReport> findAll(Pageable pageable);
}
