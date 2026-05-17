package iam.platform.bff.application.service;

import iam.platform.bff.infrastructure.client.AdminFeignClient;
import iam.platform.common.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * BFF aggregation service for admin dashboard and tenant management.
 * Aggregates data from multiple admin services for frontend consumption.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminDashboardAggregationService {

    private final AdminFeignClient adminFeignClient;

    /**
     * Get aggregated dashboard data for current user.
     * Includes: user info, tenant list, current tenant, enabled menus, available applications.
     */
    public Map<String, Object> getDashboardData(String userId, Long currentTenantId) {
        Map<String, Object> dashboardData = new HashMap<>();

        // Get user info
        try {
            ResponseEntity<UserResponse> userResponse = adminFeignClient.getUser(userId);
            if (userResponse.getBody() != null) {
                dashboardData.put("user", userResponse.getBody());
            }
        } catch (Exception e) {
            log.error("Failed to fetch user info: {}", e.getMessage());
        }

        // Get user's tenants
        List<Map<String, Object>> userTenants = new ArrayList<>();
        try {
            ResponseEntity<List<Map<String, Object>>> tenantResponse = adminFeignClient.getUserTenants(userId);
            if (tenantResponse.getBody() != null) {
                userTenants = tenantResponse.getBody();
                dashboardData.put("tenants", userTenants);
            }
        } catch (Exception e) {
            log.error("Failed to fetch user tenants: {}", e.getMessage());
        }

        // Get current tenant info
        if (currentTenantId != null) {
            dashboardData.put("currentTenantId", currentTenantId);

            // Get tenant's enabled menus
            try {
                ResponseEntity<List<Map<String, Object>>> menuResponse = adminFeignClient.getTenantMenus(currentTenantId);
                if (menuResponse.getBody() != null) {
                    dashboardData.put("menus", menuResponse.getBody());
                }
            } catch (Exception e) {
                log.error("Failed to fetch tenant menus: {}", e.getMessage());
            }

            // Get tenant's applications
            try {
                ResponseEntity<List<Map<String, Object>>> appResponse = adminFeignClient.getTenantApplications(currentTenantId);
                if (appResponse.getBody() != null) {
                    dashboardData.put("applications", appResponse.getBody());
                }
            } catch (Exception e) {
                log.error("Failed to fetch tenant applications: {}", e.getMessage());
            }
        }

        // Get platform statistics (only for platform admin tenant)
        if (isPlatformAdmin(currentTenantId)) {
            try {
                ResponseEntity<Map<String, Object>> statsResponse = adminFeignClient.getDashboardStats();
                if (statsResponse.getBody() != null) {
                    dashboardData.put("statistics", statsResponse.getBody());
                }
            } catch (Exception e) {
                log.error("Failed to fetch dashboard statistics: {}", e.getMessage());
            }
        }

        return dashboardData;
    }

    /**
     * Switch user's current tenant context.
     */
    public Map<String, Object> switchTenant(String userId, Long tenantId) {
        Map<String, String> request = new HashMap<>();
        request.put("userId", userId);
        request.put("tenantId", tenantId.toString());

        try {
            ResponseEntity<Map<String, Object>> response = adminFeignClient.switchTenant(request);
            if (response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Failed to switch tenant: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to switch tenant: " + e.getMessage());
            return errorResponse;
        }

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Tenant switch failed");
        return errorResponse;
    }

    /**
     * Check if current tenant is the platform admin tenant.
     */
    private boolean isPlatformAdmin(Long tenantId) {
        return tenantId != null && tenantId == 0L;
    }
}
