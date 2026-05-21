package iam.platform.audit.application.service;

import iam.platform.audit.domain.model.entity.AuditLog;
import iam.platform.audit.domain.repository.AuditLogRepository;
import iam.platform.common.dto.request.AuditLogQueryRequest;
import iam.platform.common.dto.response.AuditLogResponse;
import iam.platform.common.dto.response.AuditStatisticsResponse;
import iam.platform.common.dto.response.PageResponse;
import iam.platform.common.model.enums.AuditResult;
import iam.platform.common.model.enums.EventCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Audit log application service for queries and operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogApplicationService {

    private final AuditLogRepository auditLogRepository;

    public PageResponse<AuditLogResponse> queryAuditLogs(AuditLogQueryRequest request) {
        int page = request.getPage();
        int size = request.getSize();
        String sortBy = request.getSortBy() != null ? request.getSortBy() : "createdAt";
        String sortDir = request.getSortDir() != null ? request.getSortDir() : "desc";

        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<AuditLog> logsPage;
        
        // Apply filters
        if (request.getTenantId() != null) {
            logsPage = auditLogRepository.findByTenantId(request.getTenantId(), pageRequest);
        } else if (request.getUserId() != null) {
            logsPage = auditLogRepository.findByUserId(request.getUserId(), pageRequest);
        } else if (request.getEventCategory() != null) {
            logsPage = auditLogRepository.findByEventCategory(request.getEventCategory(), pageRequest);
        } else if (request.getResult() != null) {
            logsPage = auditLogRepository.findByResult(request.getResult(), pageRequest);
        } else if (request.getStartDate() != null && request.getEndDate() != null) {
            logsPage = auditLogRepository.findByCreatedAtBetween(
                    request.getStartDate(), request.getEndDate(), pageRequest);
        } else if (request.getResourceType() != null && request.getResourceId() != null) {
            logsPage = auditLogRepository.findByResourceIdAndResourceType(
                    request.getResourceId(), request.getResourceType(), pageRequest);
        } else {
            logsPage = auditLogRepository.findAll(pageRequest);
        }

        List<AuditLogResponse> content = logsPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResponse.of(content, page, size, logsPage.getTotalElements());
    }

    public Optional<AuditLogResponse> getAuditLogDetail(Long id) {
        return auditLogRepository.findById(id).map(this::toResponse);
    }

    public PageResponse<AuditLogResponse> getUserAuditLogs(Long userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuditLog> logsPage = auditLogRepository.findByUserId(userId, pageRequest);

        List<AuditLogResponse> content = logsPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResponse.of(content, page, size, logsPage.getTotalElements());
    }

    public PageResponse<AuditLogResponse> getResourceAuditLogs(String resourceType, Long resourceId, 
                                                                int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuditLog> logsPage = auditLogRepository.findByResourceIdAndResourceType(
                resourceId, resourceType, pageRequest);

        List<AuditLogResponse> content = logsPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResponse.of(content, page, size, logsPage.getTotalElements());
    }

    public AuditStatisticsResponse getStatistics(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        Map<EventCategory, Long> byCategoryEnum = auditLogRepository.countByEventCategory(
                tenantId, startDate, endDate);
        
        Map<AuditResult, Long> byResultEnum = Map.of(
                AuditResult.SUCCESS, auditLogRepository.findByResult(AuditResult.SUCCESS, 
                        PageRequest.of(0, 1)).getTotalElements(),
                AuditResult.FAILURE, auditLogRepository.findByResult(AuditResult.FAILURE, 
                        PageRequest.of(0, 1)).getTotalElements()
        );

        // Convert enum keys to string keys for the response
        Map<String, Long> byCategory = byCategoryEnum.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));
        Map<String, Long> byResult = byResultEnum.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));

        Map<String, Long> topEventTypesMap = auditLogRepository.countTopEventTypes(
                tenantId, startDate, endDate, 10);
        
        // Convert Map to List of TopEventType
        List<AuditStatisticsResponse.TopEventType> topEventTypes = topEventTypesMap.entrySet().stream()
                .map(e -> AuditStatisticsResponse.TopEventType.builder()
                        .eventType(e.getKey())
                        .count(e.getValue())
                        .build())
                .collect(Collectors.toList());

        return AuditStatisticsResponse.builder()
                .byCategory(byCategory)
                .byResult(byResult)
                .topEventTypes(topEventTypes)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    public String exportAuditLogsAsCsv(AuditLogQueryRequest request) {
        // Simplified CSV export - can be enhanced later
        PageResponse<AuditLogResponse> logs = queryAuditLogs(request);
        
        StringBuilder csv = new StringBuilder();
        csv.append("Event ID,Source Service,Username,Event Type,Category,Action,IP Address,Result,Created At\n");
        
        for (AuditLogResponse log : logs.getContent()) {
            csv.append(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                    log.getEventId(),
                    log.getSourceService(),
                    escapeCsv(log.getUsername()),
                    log.getEventType(),
                    log.getEventCategory(),
                    escapeCsv(log.getAction()),
                    log.getIpAddress(),
                    log.getResult(),
                    log.getCreatedAt()
            ));
        }
        
        return csv.toString();
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .eventId(log.getEventId())
                .sourceService(log.getSourceService())
                .tenantId(log.getTenantId())
                .userId(log.getUserId())
                .username(log.getUsername())
                .eventType(log.getEventType() != null ? log.getEventType().name() : null)
                .eventCategory(log.getEventCategory() != null ? log.getEventCategory().name() : null)
                .resourceId(log.getResourceId())
                .resourceType(log.getResourceType())
                .action(log.getAction())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .requestUri(log.getRequestUri())
                .result(log.getResult() != null ? log.getResult().name() : null)
                .errorMessage(log.getErrorMessage())
                .traceId(log.getTraceId())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
