package iam.platform.bff.interfaces.rest;

import iam.platform.bff.application.service.AdminDashboardAggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * BFF controller for admin dashboard aggregation.
 * Provides unified API for the admin UI frontend.
 */
@RestController
@RequestMapping("/bff/admin")
@RequiredArgsConstructor
public class AdminBffController {

    private final AdminDashboardAggregationService aggregationService;

    /**
     * Get aggregated dashboard data including user info, tenants, menus, and applications.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(
            @org.springframework.web.bind.annotation.RequestParam String userId,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long currentTenantId) {
        Map<String, Object> dashboardData = aggregationService.getDashboardData(userId, currentTenantId);
        return ResponseEntity.ok(dashboardData);
    }

    /**
     * Switch user's current tenant context.
     */
    @PostMapping("/tenants/switch")
    public ResponseEntity<Map<String, Object>> switchTenant(
            @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        Long tenantId = Long.parseLong(request.get("tenantId"));
        Map<String, Object> result = aggregationService.switchTenant(userId, tenantId);
        return ResponseEntity.ok(result);
    }

    /**
     * Get user's available tenants for the tenant switcher dropdown.
     */
    @GetMapping("/users/{userId}/tenants")
    public ResponseEntity<Map<String, Object>> getUserTenants(@PathVariable String userId) {
        Map<String, Object> result = aggregationService.getDashboardData(userId, null);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "tenants", result.getOrDefault("tenants", java.util.Collections.emptyList())
        ));
    }
}
