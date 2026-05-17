package iam.platform.auth.interfaces.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ConsentController {

    @GetMapping("/oauth2/consent")
    public String consent(@RequestParam(required = false) String clientName,
            @RequestParam(required = false) String scopes, org.springframework.ui.Model model) {
        model.addAttribute("clientName", clientName);
        model.addAttribute("scopes", scopes);
        return "consent";
    }
}
