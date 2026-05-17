package iam.platform.auth.interfaces.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for login page with multi-tenant support.
 *
 * Supports three tenant identification methods: 1. Subdomain: company-a.sso.example.com/login 2.
 * Query parameter: /login?tenant=company-a 3. Header: X-Tenant-Code (handled by filter)
 *
 * If tenant is not identified, user will be prompted to select one after login.
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String tenant,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout, Model model) {

        // Extract tenant code from subdomain if present
        String tenantCode = tenant;
        if (tenantCode == null || tenantCode.isBlank()) {
            tenantCode = extractFromSubdomain();
        }

        // Add tenant info to model for the view
        if (tenantCode != null && !tenantCode.isBlank()) {
            model.addAttribute("tenantCode", tenantCode);
            model.addAttribute("tenantIdentified", true);
        } else {
            model.addAttribute("tenantIdentified", false);
        }

        // Add error/logout messages
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("logout", "You have been logged out successfully");
        }

        return "login";
    }

    /**
     * Extract tenant code from request subdomain. e.g., company-a.sso.example.com -> company-a
     */
    private String extractFromSubdomain() {
        // Note: This is a placeholder. In production, this should access the actual request
        // For now, tenant identification is handled by TenantAwareAuthenticationFilter
        return null;
    }
}
