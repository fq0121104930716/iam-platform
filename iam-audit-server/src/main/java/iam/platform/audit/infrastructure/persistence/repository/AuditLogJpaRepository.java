package iam.platform.audit.infrastructure.persistence.repository;

import iam.platform.audit.infrastructure.persistence.entity.AuditLogPO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Audit log JPA repository.
 */
public interface AuditLogJpaRepository
        extends JpaRepository<AuditLogPO, Long>, JpaSpecificationExecutor<AuditLogPO> {

    Optional<AuditLogPO> findByEventId(String eventId);

    Page<AuditLogPO> findByTenantId(Long tenantId, Pageable pageable);

    Page<AuditLogPO> findByUserId(Long userId, Pageable pageable);

    Page<AuditLogPO> findByEventCategory(String eventCategory, Pageable pageable);

    Page<AuditLogPO> findByResult(String result, Pageable pageable);

    Page<AuditLogPO> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end,
            Pageable pageable);

    Page<AuditLogPO> findByResourceTypeAndResourceId(String resourceType, Long resourceId,
            Pageable pageable);

    Page<AuditLogPO> findBySourceService(String sourceService, Pageable pageable);

    @Query("SELECT a.eventCategory, COUNT(a) FROM AuditLogPO a WHERE a.tenantId = :tenantId AND a.createdAt BETWEEN :start AND :end GROUP BY a.eventCategory")
    List<Object[]> countByEventCategory(@Param("tenantId") Long tenantId,
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT event_type, COUNT(*) as cnt FROM t_audit_log WHERE tenant_id = :tenantId AND created_at BETWEEN :start AND :end GROUP BY event_type ORDER BY cnt DESC LIMIT :limit",
            nativeQuery = true)
    List<Object[]> countTopEventTypes(@Param("tenantId") Long tenantId,
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
            @Param("limit") int limit);

    int deleteByCreatedAtBefore(LocalDateTime cutoffDate);
}
