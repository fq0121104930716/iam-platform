package iam.platform.bff.interfaces.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for tenant selection page in BFF.
 */
@Controller
@RequestMapping("/bff")
public class BffTenantSelectionController {

    @GetMapping("/select-tenant")
    public String selectTenant(Model model) {
        // TODO: Fetch available tenants for current user via Feign client
        // For now, render the page without tenant data
        return "tenant-selection";
    }
}
