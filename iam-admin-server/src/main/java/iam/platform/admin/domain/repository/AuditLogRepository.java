package iam.platform.admin.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import iam.platform.admin.domain.model.entity.AuditLog;
import iam.platform.common.model.enums.AuditEventType;
import iam.platform.common.model.enums.AuditResult;
import iam.platform.common.model.enums.EventCategory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Audit log repository interface.
 */
public interface AuditLogRepository {

    // Write
    AuditLog save(AuditLog auditLog);

    void saveAll(List<AuditLog> auditLogs);

    // Read
    Optional<AuditLog> findById(Long id);

    Page<AuditLog> findByTenantId(Long tenantId, Pageable pageable);

    Page<AuditLog> findByPersonId(Long personId, Pageable pageable);

    Page<AuditLog> findByResourceIdAndResourceType(Long resourceId, String resourceType,
            Pageable pageable);

    Page<AuditLog> findByEventCategory(EventCategory category, Pageable pageable);

    Page<AuditLog> findByEventType(AuditEventType eventType, Pageable pageable);

    Page<AuditLog> findByResult(AuditResult result, Pageable pageable);

    Page<AuditLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end,
            Pageable pageable);

    // Statistics
    Map<EventCategory, Long> countByEventCategory(Long tenantId, LocalDateTime start,
            LocalDateTime end);

    Map<AuditResult, Long> countByResult(Long tenantId, LocalDateTime start, LocalDateTime end);

    Map<AuditEventType, Long> countTopEventTypes(Long tenantId, LocalDateTime start,
            LocalDateTime end, int limit);

    // Cleanup (for retention policy)
    int deleteOlderThan(LocalDateTime cutoffDate);
}
