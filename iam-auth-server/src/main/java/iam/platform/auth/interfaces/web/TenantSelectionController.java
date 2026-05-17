package iam.platform.auth.interfaces.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import iam.platform.auth.application.service.AuthenticationApplicationService;
import iam.platform.auth.application.service.AuthenticationApplicationService.TenantAccountResponse;

import java.util.List;

/**
 * Controller for tenant selection page. Displayed after login when user belongs to multiple
 * tenants.
 */
@Controller
public class TenantSelectionController {

    private final AuthenticationApplicationService authenticationApplicationService;

    public TenantSelectionController(
            AuthenticationApplicationService authenticationApplicationService) {
        this.authenticationApplicationService = authenticationApplicationService;
    }

    @GetMapping("/select-tenant")
    public String showTenantSelection(Model model) {
        try {
            List<TenantAccountResponse> tenants =
                    authenticationApplicationService.getAvailableTenants();
            model.addAttribute("tenants", tenants);

            if (tenants.isEmpty()) {
                model.addAttribute("message",
                        "You don't have access to any tenants. Please contact your administrator.");
            }
        } catch (IllegalStateException e) {
            // User not authenticated, redirect to login
            return "redirect:/login";
        }

        return "tenant-selection";
    }

    @PostMapping("/select-tenant")
    public String selectTenant(@RequestParam Long tenantAccountId, HttpServletRequest request) {
        try {
            authenticationApplicationService.selectTenant(tenantAccountId, request);
            // After tenant selection, check if there's a pending OAuth2 authorization request
            return "redirect:/";
        } catch (Exception e) {
            return "redirect:/select-tenant?error=true";
        }
    }
}
