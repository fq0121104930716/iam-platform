package iam.platform.admin.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import iam.platform.common.dto.response.AppStatisticsResponse;
import iam.platform.common.dto.response.AuditStatisticsResponse;
import iam.platform.common.dto.response.DashboardOverviewResponse;
import iam.platform.common.dto.response.TenantOverviewResponse;
import iam.platform.common.dto.response.PersonStatisticsResponse;
import iam.platform.admin.application.service.AuditApplicationService;
import iam.platform.admin.application.service.DashboardApplicationService;
import iam.platform.common.model.annotation.RequirePermission;
import iam.platform.common.api.ApiResponse;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "管理控制台统计 API")
public class DashboardController {

    private final DashboardApplicationService dashboardService;
    private final AuditApplicationService auditApplicationService;

    @GetMapping("/overview")
    @Operation(summary = "全局概览", description = "获取平台级别的租户、用户、应用统计")
    @RequirePermission("dashboard:read")
    public ApiResponse<DashboardOverviewResponse> getOverview() {
        return ApiResponse.success(dashboardService.getGlobalOverview());
    }

    @GetMapping("/tenants/{id}/overview")
    @Operation(summary = "租户概览", description = "获取指定租户的详细统计信息")
    @RequirePermission("dashboard:read")
    public ApiResponse<TenantOverviewResponse> getTenantOverview(@PathVariable Long id) {
        return ApiResponse.success(dashboardService.getTenantOverview(id));
    }

    @GetMapping("/statistics/persons")
    @Operation(summary = "自然人统计", description = "获取自然人增长和活跃度统计")
    @RequirePermission("dashboard:read")
    public ApiResponse<PersonStatisticsResponse> getPersonStatistics(
            @RequestParam(required = false) Long tenantId) {
        return ApiResponse.success(dashboardService.getPersonStatistics(tenantId));
    }

    @GetMapping("/statistics/apps")
    @Operation(summary = "应用统计", description = "获取应用分布和授权统计")
    @RequirePermission("dashboard:read")
    public ApiResponse<AppStatisticsResponse> getAppStatistics(
            @RequestParam(required = false) Long tenantId) {
        return ApiResponse.success(dashboardService.getAppStatistics(tenantId));
    }

    @GetMapping("/statistics/audit")
    @Operation(summary = "审计统计", description = "获取审计日志统计数据")
    @RequirePermission("audit:read")
    public ApiResponse<AuditStatisticsResponse> getAuditStatistics(@RequestParam Long tenantId,
            @RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) {
        return ApiResponse
                .success(auditApplicationService.getStatistics(tenantId, startDate, endDate));
    }
}
