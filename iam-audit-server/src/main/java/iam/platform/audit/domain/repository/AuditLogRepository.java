package iam.platform.audit.domain.repository;

import iam.platform.audit.domain.model.entity.AuditLog;
import iam.platform.common.model.enums.AuditResult;
import iam.platform.common.model.enums.EventCategory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Audit log repository interface (domain layer).
 */
public interface AuditLogRepository {

    AuditLog save(AuditLog auditLog);

    Optional<AuditLog> findById(Long id);

    Optional<AuditLog> findByEventId(String eventId);

    Page<AuditLog> findByTenantId(Long tenantId, Pageable pageable);

    Page<AuditLog> findByPersonId(Long personId, Pageable pageable);

    Page<AuditLog> findByEventCategory(EventCategory category, Pageable pageable);

    Page<AuditLog> findByResult(AuditResult result, Pageable pageable);

    Page<AuditLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<AuditLog> findByResourceIdAndResourceType(Long resourceId, String resourceType, Pageable pageable);

    Page<AuditLog> findBySourceService(String sourceService, Pageable pageable);

    Page<AuditLog> findAll(Pageable pageable);

    Map<EventCategory, Long> countByEventCategory(Long tenantId, LocalDateTime start, LocalDateTime end);

    Map<String, Long> countTopEventTypes(Long tenantId, LocalDateTime start, LocalDateTime end, int limit);

    int deleteOlderThan(LocalDateTime cutoffDate);
}
