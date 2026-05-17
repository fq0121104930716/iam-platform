package iam.platform.bff.interfaces.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for OAuth2 consent page in BFF.
 */
@Controller
@RequestMapping("/bff")
public class BffConsentController {

    @GetMapping("/consent")
    public String consent(@RequestParam(required = false) String clientName,
            @RequestParam(required = false) String scopes,
            @RequestParam(required = false) String clientId,
            Model model) {

        if (clientName != null) {
            model.addAttribute("clientName", clientName);
        }
        if (scopes != null) {
            model.addAttribute("scopes", scopes);
        }
        if (clientId != null) {
            model.addAttribute("clientId", clientId);
        }

        return "consent";
    }
}
