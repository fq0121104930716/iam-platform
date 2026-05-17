package iam.platform.bff.interfaces.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for BFF home page.
 * After successful OAuth2 login, users are redirected here.
 */
@Controller
@RequestMapping("/bff")
public class BffHomeController {

    @GetMapping("/")
    public String home() {
        // Redirect to login page for now
        // In the future, this can show a dashboard or welcome page for authenticated users
        return "redirect:/bff/login";
    }
}
