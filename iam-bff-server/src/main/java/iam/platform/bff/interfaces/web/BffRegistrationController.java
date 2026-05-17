package iam.platform.bff.interfaces.web;

import iam.platform.bff.application.service.BffRegistrationService;
import iam.platform.common.dto.request.CreatePersonRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for registration page in BFF.
 */
@Controller
@RequestMapping("/bff")
@RequiredArgsConstructor
public class BffRegistrationController {

    private final BffRegistrationService registrationService;

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("personRequest", new CreatePersonRequest());
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@ModelAttribute @Validated CreatePersonRequest request,
            Model model) {
        try {
            registrationService.registerPerson(request);
            return "redirect:/bff/login?registered";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            model.addAttribute("personRequest", request);
            return "register";
        }
    }
}
