package iam.platform.audit.infrastructure.persistence.impl;

import iam.platform.audit.domain.model.entity.AuditLog;
import iam.platform.audit.domain.repository.AuditLogRepository;
import iam.platform.audit.infrastructure.persistence.converter.AuditLogConverter;
import iam.platform.audit.infrastructure.persistence.entity.AuditLogPO;
import iam.platform.audit.infrastructure.persistence.repository.AuditLogJpaRepository;
import iam.platform.common.model.enums.AuditResult;
import iam.platform.common.model.enums.EventCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Audit log repository implementation.
 */
@Repository
@RequiredArgsConstructor
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private final AuditLogJpaRepository jpaRepository;
    private final AuditLogConverter converter;

    @Override
    public AuditLog save(AuditLog auditLog) {
        AuditLogPO po = converter.toPO(auditLog);
        AuditLogPO savedPo = jpaRepository.save(po);
        return converter.toDomain(savedPo);
    }

    @Override
    public Optional<AuditLog> findById(Long id) {
        return jpaRepository.findById(id).map(converter::toDomain);
    }

    @Override
    public Optional<AuditLog> findByEventId(String eventId) {
        return jpaRepository.findByEventId(eventId).map(converter::toDomain);
    }

    @Override
    public Page<AuditLog> findByTenantId(Long tenantId, Pageable pageable) {
        return jpaRepository.findByTenantId(tenantId, pageable).map(converter::toDomain);
    }

    @Override
    public Page<AuditLog> findByUserId(Long userId, Pageable pageable) {
        return jpaRepository.findByUserId(userId, pageable).map(converter::toDomain);
    }

    @Override
    public Page<AuditLog> findByEventCategory(EventCategory category, Pageable pageable) {
        return jpaRepository.findByEventCategory(category.name(), pageable)
                .map(converter::toDomain);
    }

    @Override
    public Page<AuditLog> findByResult(AuditResult result, Pageable pageable) {
        return jpaRepository.findByResult(result.name(), pageable).map(converter::toDomain);
    }

    @Override
    public Page<AuditLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end,
            Pageable pageable) {
        return jpaRepository.findByCreatedAtBetween(start, end, pageable).map(converter::toDomain);
    }

    @Override
    public Page<AuditLog> findByResourceIdAndResourceType(Long resourceId, String resourceType,
            Pageable pageable) {
        return jpaRepository.findByResourceTypeAndResourceId(resourceType, resourceId, pageable)
                .map(converter::toDomain);
    }

    @Override
    public Page<AuditLog> findBySourceService(String sourceService, Pageable pageable) {
        return jpaRepository.findBySourceService(sourceService, pageable).map(converter::toDomain);
    }

    @Override
    public Page<AuditLog> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(converter::toDomain);
    }

    @Override
    public Map<EventCategory, Long> countByEventCategory(Long tenantId, LocalDateTime start,
            LocalDateTime end) {
        List<Object[]> results = jpaRepository.countByEventCategory(tenantId, start, end);
        Map<EventCategory, Long> map = new LinkedHashMap<>();
        for (Object[] row : results) {
            String category = (String) row[0];
            Long count = (Long) row[1];
            try {
                map.put(EventCategory.valueOf(category), count);
            } catch (IllegalArgumentException e) {
                // Skip unknown categories
            }
        }
        return map;
    }

    @Override
    public Map<String, Long> countTopEventTypes(Long tenantId, LocalDateTime start,
            LocalDateTime end, int limit) {
        List<Object[]> results = jpaRepository.countTopEventTypes(tenantId, start, end, limit);
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : results) {
            String eventType = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            map.put(eventType, count);
        }
        return map;
    }

    @Override
    public int deleteOlderThan(LocalDateTime cutoffDate) {
        return jpaRepository.deleteByCreatedAtBefore(cutoffDate);
    }
}
