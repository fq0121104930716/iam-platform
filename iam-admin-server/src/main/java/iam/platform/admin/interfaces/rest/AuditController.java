package iam.platform.admin.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import iam.platform.common.dto.request.AuditLogQueryRequest;
import iam.platform.common.dto.response.AuditLogResponse;
import iam.platform.common.dto.response.AuditStatisticsResponse;
import iam.platform.admin.application.service.AuditApplicationService;
import iam.platform.common.api.ApiResponse;
import iam.platform.common.api.PageResponse;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/v1/audit")
@RequiredArgsConstructor
@Tag(name = "Audit", description = "Audit log management API")
public class AuditController {

    private final AuditApplicationService auditApplicationService;

    @GetMapping("/logs")
    @Operation(summary = "Query audit logs with pagination and filters")
    public ApiResponse<PageResponse<AuditLogResponse>> listLogs(
            @ModelAttribute AuditLogQueryRequest request) {
        return ApiResponse.success(auditApplicationService.queryAuditLogs(request));
    }

    @GetMapping("/logs/{id}")
    @Operation(summary = "Get audit log detail by ID")
    public ApiResponse<AuditLogResponse> getLogDetail(@PathVariable Long id) {
        return ApiResponse.success(auditApplicationService.getAuditLogDetail(id));
    }

    @GetMapping("/users/{userId}/logs")
    @Operation(summary = "Query audit logs by user")
    public ApiResponse<PageResponse<AuditLogResponse>> getUserLogs(@PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(auditApplicationService.getUserAuditLogs(userId, page, size));
    }

    @GetMapping("/resources/{resourceType}/{resourceId}/logs")
    @Operation(summary = "Query audit logs by resource")
    public ApiResponse<PageResponse<AuditLogResponse>> getResourceLogs(
            @PathVariable String resourceType, @PathVariable Long resourceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(
                auditApplicationService.getResourceAuditLogs(resourceType, resourceId, page, size));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get audit statistics for a time range")
    public ApiResponse<AuditStatisticsResponse> getStatistics(@RequestParam Long tenantId,
            @RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) {
        return ApiResponse
                .success(auditApplicationService.getStatistics(tenantId, startDate, endDate));
    }

    @PostMapping(value = "/logs/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Operation(summary = "Export audit logs as CSV")
    public void exportLogs(@RequestBody AuditLogQueryRequest request, HttpServletResponse response)
            throws IOException {
        byte[] csvData = auditApplicationService.exportAuditLogsAsCsv(request);

        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=audit-logs-"
                        + LocalDateTime.now().format(
                                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
                        + ".csv");
        response.setContentLength(csvData.length);
        response.getOutputStream().write(csvData);
    }
}
