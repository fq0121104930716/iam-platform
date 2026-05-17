package iam.platform.admin.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import iam.platform.common.dto.response.AppStatisticsResponse;
import iam.platform.common.dto.response.DashboardOverviewResponse;
import iam.platform.common.dto.response.TenantOverviewResponse;
import iam.platform.common.dto.response.UserStatisticsResponse;
import iam.platform.admin.domain.model.entity.Application;
import iam.platform.admin.domain.model.entity.Tenant;
import iam.platform.common.model.enums.TenantStatus;
import iam.platform.admin.domain.repository.ApplicationRepository;
import iam.platform.admin.domain.repository.OrganizationRepository;
import iam.platform.admin.domain.repository.UserRepository;
import iam.platform.admin.domain.repository.TenantAccountRepository;
import iam.platform.admin.domain.repository.TenantRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardApplicationService {

        private final TenantRepository tenantRepository;
        private final TenantAccountRepository tenantAccountRepository;
        private final ApplicationRepository applicationRepository;
        private final UserRepository UserRepository;
        private final OrganizationRepository organizationRepository;

        /**
         * 获取全局概览统计
         */
        public DashboardOverviewResponse getGlobalOverview() {
                long totalTenants = tenantRepository.countByStatus(TenantStatus.ACTIVE.name())
                                + tenantRepository.countByStatus(TenantStatus.SUSPENDED.name());
                long activeTenants = tenantRepository.countByStatus(TenantStatus.ACTIVE.name());

                long totalUsers = UserRepository.countByEnabledTrue();
                long activeUsers = totalUsers; // enabled = active

                long totalApplications = applicationRepository.countByStatus("ACTIVE")
                                + applicationRepository.countByStatus("INACTIVE")
                                + applicationRepository.countByStatus("REVIEWING")
                                + applicationRepository.countByStatus("BLOCKED");
                long activeApplications = applicationRepository.countByStatus("ACTIVE");

                return DashboardOverviewResponse.builder().totalTenants(totalTenants)
                                .activeTenants(activeTenants).totalUsers(totalUsers)
                                .activeUsers(activeUsers).totalApplications(totalApplications)
                                .activeApplications(activeApplications).build();
        }

        /**
         * 获取租户特定概览统计
         */
        public TenantOverviewResponse getTenantOverview(Long tenantId) {
                Tenant tenant = tenantRepository.findById(tenantId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Tenant not found: " + tenantId));

                long userCount = tenantAccountRepository.countByTenantIdAndStatus(tenantId,
                                "ACTIVE")
                                + tenantAccountRepository.countByTenantIdAndStatus(tenantId,
                                                "SUSPENDED");
                long activeUserCount = tenantAccountRepository.countByTenantIdAndStatus(tenantId,
                                "ACTIVE");

                long applicationCount =
                                applicationRepository.countByTenantIdAndStatus(tenantId, "ACTIVE")
                                                + applicationRepository.countByTenantIdAndStatus(
                                                                tenantId, "INACTIVE")
                                                + applicationRepository.countByTenantIdAndStatus(
                                                                tenantId, "REVIEWING")
                                                + applicationRepository.countByTenantIdAndStatus(
                                                                tenantId, "BLOCKED");
                long activeApplicationCount =
                                applicationRepository.countByTenantIdAndStatus(tenantId, "ACTIVE");

                long organizationCount = organizationRepository.countByTenantId(tenantId);

                return TenantOverviewResponse.builder().tenantId(tenant.getId())
                                .tenantCode(tenant.getTenantCode())
                                .tenantName(tenant.getTenantName())
                                .status(tenant.getStatus().name()).userCount(userCount)
                                .activeUserCount(activeUserCount).applicationCount(applicationCount)
                                .activeApplicationCount(activeApplicationCount)
                                .organizationCount(organizationCount)
                                .expiresAt(tenant.getExpiresAt()).build();
        }

        /**
         * 获取用户统计
         */
        public UserStatisticsResponse getUserStatistics(Long tenantId) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
                LocalDateTime weekStart = todayStart.minusDays(now.getDayOfWeek().getValue() - 1);
                LocalDateTime monthStart = todayStart.withDayOfMonth(1);

                long totalUsers = UserRepository.countByEnabledTrue();
                long activeUsers = totalUsers;

                long newUsersToday = UserRepository.countByCreatedAtBetween(todayStart, now);
                long newUsersThisWeek = UserRepository.countByCreatedAtBetween(weekStart, now);
                long newUsersThisMonth =
                                UserRepository.countByCreatedAtBetween(monthStart, now);

                // 简化版：实际应从审计日志计算登录成功率
                double loginSuccessRate = 95.0; // 默认值
                long totalLoginsToday = 0;
                long failedLoginsToday = 0;

                return UserStatisticsResponse.builder().totalUsers(totalUsers)
                                .activeUsers(activeUsers).newUsersToday(newUsersToday)
                                .newUsersThisWeek(newUsersThisWeek)
                                .newUsersThisMonth(newUsersThisMonth)
                                .loginSuccessRate(loginSuccessRate)
                                .totalLoginsToday(totalLoginsToday)
                                .failedLoginsToday(failedLoginsToday).build();
        }

        /**
         * 获取应用统计
         */
        public AppStatisticsResponse getAppStatistics(Long tenantId) {
                List<Application> applications;
                if (tenantId != null) {
                        applications = applicationRepository.findByTenantId(tenantId);
                } else {
                        applications = applicationRepository.findAll();
                }

                long totalApplications = applications.size();
                long activeApplications = applications.stream().filter(app -> "ACTIVE"
                                .equals(app.getStatus() != null ? app.getStatus().name() : null))
                                .count();
                long inactiveApplications = applications.stream().filter(app -> "INACTIVE"
                                .equals(app.getStatus() != null ? app.getStatus().name() : null))
                                .count();

                // 按类型分组统计
                Map<String, Long> countByType = applications.stream().collect(Collectors.groupingBy(
                                app -> app.getAppType() != null ? app.getAppType().name()
                                                : "UNKNOWN",
                                Collectors.counting()));

                return AppStatisticsResponse.builder().totalApplications(totalApplications)
                                .activeApplications(activeApplications)
                                .inactiveApplications(inactiveApplications)
                                .countByType(new HashMap<>(countByType)).totalAuthorizations(0L)
                                .build();
        }
}
