package iam.platform.admin.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import iam.platform.admin.application.assembler.AuditLogAssembler;
import iam.platform.common.dto.request.AuditLogQueryRequest;
import iam.platform.common.dto.response.AuditLogResponse;
import iam.platform.common.dto.response.AuditStatisticsResponse;
import iam.platform.admin.domain.model.entity.AuditLog;
import iam.platform.common.model.enums.AuditEventType;
import iam.platform.common.model.enums.EventCategory;
import iam.platform.admin.domain.repository.AuditLogRepository;
import iam.platform.common.api.PageResponse;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Application service for audit log queries, statistics, and export.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditApplicationService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogAssembler auditLogAssembler;

    /**
     * Save an audit log entry (called by async event listener).
     */
    public void saveAuditLog(AuditLog auditLog) {
        try {
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Audit log write failure should not affect the main business flow
            log.error("Failed to save audit log: {}", e.getMessage(), e);
        }
    }

    /**
     * Query audit logs with pagination and filters.
     */
    public PageResponse<AuditLogResponse> queryAuditLogs(AuditLogQueryRequest request) {
        Sort sort = Sort.by("desc".equalsIgnoreCase(request.getSortDir()) ? Sort.Direction.DESC
                : Sort.Direction.ASC, request.getSortBy());
        PageRequest pageRequest = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<AuditLog> auditLogPage;

        if (request.getTenantId() != null) {
            auditLogPage = auditLogRepository.findByTenantId(request.getTenantId(), pageRequest);
        } else if (request.getUserId() != null) {
            auditLogPage = auditLogRepository.findByUserId(request.getUserId(), pageRequest);
        } else if (request.getEventType() != null) {
            auditLogPage = auditLogRepository.findByEventType(request.getEventType(), pageRequest);
        } else if (request.getEventCategory() != null) {
            auditLogPage =
                    auditLogRepository.findByEventCategory(request.getEventCategory(), pageRequest);
        } else if (request.getResourceType() != null && request.getResourceId() != null) {
            auditLogPage = auditLogRepository.findByResourceIdAndResourceType(
                    request.getResourceId(), request.getResourceType(), pageRequest);
        } else if (request.getStartDate() != null && request.getEndDate() != null) {
            auditLogPage = auditLogRepository.findByCreatedAtBetween(request.getStartDate(),
                    request.getEndDate(), pageRequest);
        } else {
            // Default: return empty page, require at least one filter
            auditLogPage = Page.empty(pageRequest);
        }

        List<AuditLogResponse> responses =
                auditLogPage.getContent().stream().map(auditLogAssembler::toResponse).toList();

        return PageResponse.of(responses, request.getPage(), request.getSize(),
                auditLogPage.getTotalElements());
    }

    /**
     * Get detail of a single audit log entry.
     */
    public AuditLogResponse getAuditLogDetail(Long id) {
        AuditLog auditLog = auditLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Audit log not found: " + id));
        return auditLogAssembler.toResponse(auditLog);
    }

    /**
     * Query audit logs by user.
     */
    public PageResponse<AuditLogResponse> getUserAuditLogs(Long userId, int page, int size) {
        PageRequest pageRequest =
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> auditLogPage = auditLogRepository.findByUserId(userId, pageRequest);

        List<AuditLogResponse> responses =
                auditLogPage.getContent().stream().map(auditLogAssembler::toResponse).toList();

        return PageResponse.of(responses, page, size, auditLogPage.getTotalElements());
    }

    /**
     * Query audit logs by resource.
     */
    public PageResponse<AuditLogResponse> getResourceAuditLogs(String resourceType, Long resourceId,
            int page, int size) {
        PageRequest pageRequest =
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AuditLog> auditLogPage = auditLogRepository.findByResourceIdAndResourceType(resourceId,
                resourceType, pageRequest);

        List<AuditLogResponse> responses =
                auditLogPage.getContent().stream().map(auditLogAssembler::toResponse).toList();

        return PageResponse.of(responses, page, size, auditLogPage.getTotalElements());
    }

    /**
     * Get audit statistics for a given time range.
     */
    public AuditStatisticsResponse getStatistics(Long tenantId, LocalDateTime startDate,
            LocalDateTime endDate) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID is required for statistics");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "Start date and end date are required for statistics");
        }

        Map<EventCategory, Long> categoryCounts =
                auditLogRepository.countByEventCategory(tenantId, startDate, endDate);
        Map<iam.platform.common.model.enums.AuditResult, Long> resultCounts =
                auditLogRepository.countByResult(tenantId, startDate, endDate);
        Map<AuditEventType, Long> topEventTypes =
                auditLogRepository.countTopEventTypes(tenantId, startDate, endDate, 10);

        long totalLogs = categoryCounts.values().stream().mapToLong(Long::longValue).sum();

        return AuditStatisticsResponse.builder().totalLogs(totalLogs)
                .byCategory(categoryCounts.entrySet().stream()
                        .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue)))
                .byResult(resultCounts.entrySet().stream()
                        .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue)))
                .topEventTypes(topEventTypes.entrySet().stream()
                        .map(e -> AuditStatisticsResponse.TopEventType.builder()
                                .eventType(e.getKey().name()).count(e.getValue()).build())
                        .toList())
                .startDate(startDate).endDate(endDate).build();
    }

    /**
     * Export audit logs as CSV.
     */
    public byte[] exportAuditLogsAsCsv(AuditLogQueryRequest request) {
        // Query all matching logs (no pagination limit)
        List<AuditLog> allLogs = queryAllLogs(request);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(baos, true, StandardCharsets.UTF_8)) {
            // Header
            writer.println(
                    "ID,TenantID,PersonID,Username,EventType,EventCategory,ResourceType,ResourceID,Action,IPAddress,UserAgent,RequestURI,Result,ErrorMessage,CreatedAt");

            // Data rows
            for (AuditLog log : allLogs) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                        csvEscape(log.getId()), csvEscape(log.getTenantId()),
                        csvEscape(log.getUserId()), csvEscape(log.getUsername()),
                        csvEscape(log.getEventType()), csvEscape(log.getEventCategory()),
                        csvEscape(log.getResourceType()), csvEscape(log.getResourceId()),
                        csvEscape(log.getAction()), csvEscape(log.getIpAddress()),
                        csvEscape(log.getUserAgent()), csvEscape(log.getRequestUri()),
                        csvEscape(log.getResult()), csvEscape(log.getErrorMessage()),
                        csvEscape(log.getCreatedAt()));
            }
        }
        return baos.toByteArray();
    }

    private List<AuditLog> queryAllLogs(AuditLogQueryRequest request) {
        // Simplified: query first 10000 logs (for export limitation)
        PageRequest pageRequest =
                PageRequest.of(0, 10000, Sort.by(Sort.Direction.DESC, "createdAt"));

        if (request.getTenantId() != null) {
            return auditLogRepository.findByTenantId(request.getTenantId(), pageRequest)
                    .getContent();
        } else if (request.getUserId() != null) {
            return auditLogRepository.findByUserId(request.getUserId(), pageRequest).getContent();
        } else if (request.getStartDate() != null && request.getEndDate() != null) {
            return auditLogRepository.findByCreatedAtBetween(request.getStartDate(),
                    request.getEndDate(), pageRequest).getContent();
        }
        return List.of();
    }

    private String csvEscape(Object obj) {
        if (obj == null)
            return "";
        String str = obj.toString();
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }
}
