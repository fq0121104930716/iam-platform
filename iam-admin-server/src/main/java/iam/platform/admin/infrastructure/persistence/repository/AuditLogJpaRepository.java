package iam.platform.admin.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import iam.platform.common.model.enums.AuditEventType;
import iam.platform.common.model.enums.AuditResult;
import iam.platform.common.model.enums.EventCategory;
import iam.platform.admin.infrastructure.persistence.entity.AuditLogPO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for audit log.
 */
public interface AuditLogJpaRepository
        extends JpaRepository<AuditLogPO, Long>, JpaSpecificationExecutor<AuditLogPO> {

    Page<AuditLogPO> findByTenantId(Long tenantId, Pageable pageable);

    Page<AuditLogPO> findByPersonId(Long personId, Pageable pageable);

    Page<AuditLogPO> findByEventCategory(EventCategory category, Pageable pageable);

    Page<AuditLogPO> findByEventType(AuditEventType eventType, Pageable pageable);

    Page<AuditLogPO> findByResult(AuditResult result, Pageable pageable);

    Page<AuditLogPO> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end,
            Pageable pageable);

    Page<AuditLogPO> findByResourceTypeAndResourceId(String resourceType, Long resourceId,
            Pageable pageable);

    @Query("SELECT a.eventCategory, COUNT(a) FROM AuditLogPO a WHERE a.tenantId = :tenantId AND a.createdAt BETWEEN :start AND :end GROUP BY a.eventCategory")
    List<Object[]> countByEventCategory(@Param("tenantId") Long tenantId,
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT a.result, COUNT(a) FROM AuditLogPO a WHERE a.tenantId = :tenantId AND a.createdAt BETWEEN :start AND :end GROUP BY a.result")
    List<Object[]> countByResult(@Param("tenantId") Long tenantId,
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT a.eventType, COUNT(a) FROM AuditLogPO a WHERE a.tenantId = :tenantId AND a.createdAt BETWEEN :start AND :end GROUP BY a.eventType ORDER BY COUNT(a) DESC")
    List<Object[]> findTopEventTypes(@Param("tenantId") Long tenantId,
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
            Pageable pageable);

    @Modifying
    @Query("DELETE FROM AuditLogPO a WHERE a.createdAt < :cutoffDate")
    int deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}
