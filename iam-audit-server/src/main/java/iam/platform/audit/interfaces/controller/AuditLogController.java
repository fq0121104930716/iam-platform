package iam.platform.audit.interfaces.controller;

import iam.platform.audit.application.service.AuditLogApplicationService;
import iam.platform.common.dto.request.AuditLogQueryRequest;
import iam.platform.common.dto.response.AuditLogResponse;
import iam.platform.common.dto.response.AuditStatisticsResponse;
import iam.platform.common.dto.response.PageResponse;
import iam.platform.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for audit log queries.
 */
@RestController
@RequestMapping("/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Audit log query and export APIs")
public class AuditLogController {

    private final AuditLogApplicationService auditLogService;

    @GetMapping("/logs")
    @Operation(summary = "Query audit logs", description = "Query audit logs with filters and pagination")
    public ApiResponse<PageResponse<AuditLogResponse>> listLogs(@ModelAttribute AuditLogQueryRequest request) {
        return ApiResponse.success(auditLogService.queryAuditLogs(request));
    }

    @GetMapping("/logs/{id}")
    @Operation(summary = "Get audit log detail", description = "Get single audit log by ID")
    public ApiResponse<AuditLogResponse> getLog(@PathVariable Long id) {
        return auditLogService.getAuditLogDetail(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error(404, "Audit log not found: " + id, null));
    }

    @GetMapping("/users/{userId}/logs")
    @Operation(summary = "Get user audit logs", description = "Get audit logs for specific user")
    public ApiResponse<PageResponse<AuditLogResponse>> getUserLogs(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(auditLogService.getUserAuditLogs(userId, page, size));
    }

    @GetMapping("/resources/{resourceType}/{resourceId}/logs")
    @Operation(summary = "Get resource audit logs", description = "Get audit logs for specific resource")
    public ApiResponse<PageResponse<AuditLogResponse>> getResourceLogs(
            @PathVariable String resourceType,
            @PathVariable Long resourceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(auditLogService.getResourceAuditLogs(resourceType, resourceId, page, size));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get audit statistics", description = "Get audit log statistics for date range")
    public ApiResponse<AuditStatisticsResponse> getStatistics(
            @RequestParam Long tenantId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        return ApiResponse.success(auditLogService.getStatistics(
                tenantId, 
                java.time.LocalDateTime.parse(startDate), 
                java.time.LocalDateTime.parse(endDate)));
    }

    @PostMapping("/logs/export")
    @Operation(summary = "Export audit logs as CSV", description = "Export filtered audit logs as CSV file")
    public ResponseEntity<byte[]> exportLogs(@RequestBody AuditLogQueryRequest request) {
        String csv = auditLogService.exportAuditLogsAsCsv(request);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit_logs.csv\"");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csv.getBytes());
    }
}
