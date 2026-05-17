package iam.platform.bff.interfaces.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for login page in BFF.
 * Renders the login view without handling authentication logic.
 * Authentication is handled by gateway and auth-server.
 */
@Controller
@RequestMapping("/bff")
public class BffLoginController {

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String tenant,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            @RequestParam(required = false) String registered,
            Model model) {

        // Extract tenant code from parameter if present
        String tenantCode = tenant;

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
        if (registered != null) {
            model.addAttribute("registered", "Registration successful. Please login.");
        }

        return "login";
    }
}
