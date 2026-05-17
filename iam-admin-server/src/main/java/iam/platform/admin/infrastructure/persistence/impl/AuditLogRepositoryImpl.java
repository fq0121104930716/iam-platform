package iam.platform.admin.infrastructure.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import iam.platform.admin.domain.model.entity.AuditLog;
import iam.platform.common.model.enums.AuditEventType;
import iam.platform.common.model.enums.AuditResult;
import iam.platform.common.model.enums.EventCategory;
import iam.platform.admin.domain.repository.AuditLogRepository;
import iam.platform.admin.infrastructure.persistence.entity.AuditLogPO;
import iam.platform.admin.infrastructure.persistence.repository.AuditLogJpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of AuditLogRepository.
 */
@Repository
@RequiredArgsConstructor
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private final AuditLogJpaRepository jpaRepository;

    @Override
    @Transactional
    public AuditLog save(AuditLog auditLog) {
        AuditLogPO po = toPO(auditLog);
        po = jpaRepository.save(po);
        return toDomain(po);
    }

    @Override
    @Transactional
    public void saveAll(List<AuditLog> auditLogs) {
        List<AuditLogPO> pos = auditLogs.stream().map(this::toPO).toList();
        jpaRepository.saveAll(pos);
    }

    @Override
    public Optional<AuditLog> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Page<AuditLog> findByTenantId(Long tenantId, Pageable pageable) {
        return jpaRepository.findByTenantId(tenantId, pageable).map(this::toDomain);
    }

    @Override
    public Page<AuditLog> findByUserId(Long userId, Pageable pageable) {
        return jpaRepository.findByUserId(userId, pageable).map(this::toDomain);
    }

    @Override
    public Page<AuditLog> findByResourceIdAndResourceType(Long resourceId, String resourceType,
            Pageable pageable) {
        return jpaRepository.findByResourceTypeAndResourceId(resourceType, resourceId, pageable)
                .map(this::toDomain);
    }

    @Override
    public Page<AuditLog> findByEventCategory(EventCategory category, Pageable pageable) {
        return jpaRepository.findByEventCategory(category, pageable).map(this::toDomain);
    }

    @Override
    public Page<AuditLog> findByEventType(AuditEventType eventType, Pageable pageable) {
        return jpaRepository.findByEventType(eventType, pageable).map(this::toDomain);
    }

    @Override
    public Page<AuditLog> findByResult(AuditResult result, Pageable pageable) {
        return jpaRepository.findByResult(result, pageable).map(this::toDomain);
    }

    @Override
    public Page<AuditLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end,
            Pageable pageable) {
        return jpaRepository.findByCreatedAtBetween(start, end, pageable).map(this::toDomain);
    }

    @Override
    public Map<EventCategory, Long> countByEventCategory(Long tenantId, LocalDateTime start,
            LocalDateTime end) {
        List<Object[]> results = jpaRepository.countByEventCategory(tenantId, start, end);
        return results.stream()
                .collect(Collectors.toMap(row -> (EventCategory) row[0], row -> (Long) row[1]));
    }

    @Override
    public Map<AuditResult, Long> countByResult(Long tenantId, LocalDateTime start,
            LocalDateTime end) {
        List<Object[]> results = jpaRepository.countByResult(tenantId, start, end);
        return results.stream()
                .collect(Collectors.toMap(row -> (AuditResult) row[0], row -> (Long) row[1]));
    }

    @Override
    public Map<AuditEventType, Long> countTopEventTypes(Long tenantId, LocalDateTime start,
            LocalDateTime end, int limit) {
        Pageable topPage = org.springframework.data.domain.PageRequest.of(0, limit);
        List<Object[]> results = jpaRepository.findTopEventTypes(tenantId, start, end, topPage);
        return results.stream()
                .collect(Collectors.toMap(row -> (AuditEventType) row[0], row -> (Long) row[1]));
    }

    @Override
    @Transactional
    public int deleteOlderThan(LocalDateTime cutoffDate) {
        return jpaRepository.deleteOlderThan(cutoffDate);
    }

    private AuditLogPO toPO(AuditLog domain) {
        AuditLogPO po = new AuditLogPO();
        po.setId(domain.getId());
        po.setEventId(domain.getEventId());
        po.setSourceService(domain.getSourceService());
        po.setTenantId(domain.getTenantId());
        po.setUserId(domain.getUserId());
        po.setUsername(domain.getUsername());
        po.setEventType(domain.getEventType());
        po.setEventCategory(domain.getEventCategory());
        po.setResourceId(domain.getResourceId());
        po.setResourceType(domain.getResourceType());
        po.setAction(domain.getAction());
        po.setIpAddress(domain.getIpAddress());
        po.setUserAgent(domain.getUserAgent());
        po.setRequestUri(domain.getRequestUri());
        po.setRequestParams(domain.getRequestParams());
        po.setResult(domain.getResult());
        po.setErrorMessage(domain.getErrorMessage());
        po.setTraceId(domain.getTraceId());
        po.setCreatedAt(domain.getCreatedAt());
        return po;
    }

    private AuditLog toDomain(AuditLogPO po) {
        return AuditLog.builder().id(po.getId()).eventId(po.getEventId())
                .sourceService(po.getSourceService()).tenantId(po.getTenantId())
                .userId(po.getUserId()).username(po.getUsername()).eventType(po.getEventType())
                .eventCategory(po.getEventCategory()).resourceId(po.getResourceId())
                .resourceType(po.getResourceType()).action(po.getAction())
                .ipAddress(po.getIpAddress()).userAgent(po.getUserAgent())
                .requestUri(po.getRequestUri()).requestParams(po.getRequestParams())
                .result(po.getResult()).errorMessage(po.getErrorMessage()).traceId(po.getTraceId())
                .createdAt(po.getCreatedAt()).build();
    }
}
