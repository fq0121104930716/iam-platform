package iam.platform.bff.interfaces.web;

import iam.platform.bff.application.service.AdminDashboardAggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Controller for tenant selection page in BFF.
 * Displays available tenants for the user to choose from.
 */
@Controller
@RequestMapping("/bff")
@RequiredArgsConstructor
public class BffTenantSelectionController {

    private final AdminDashboardAggregationService aggregationService;

    @GetMapping("/select-tenant")
    public String selectTenant(
            @RequestParam String userId,
            Model model) {
        // Fetch user's available tenants
        Map<String, Object> dashboardData = aggregationService.getDashboardData(userId, null);
        List<Map<String, Object>> tenants = (List<Map<String, Object>>) dashboardData.get("tenants");

        model.addAttribute("userId", userId);
        model.addAttribute("tenants", tenants != null ? tenants : List.of());

        return "tenant-selection";
    }
}
