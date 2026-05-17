package iam.platform.bff.interfaces.web;

import iam.platform.bff.application.service.BffRegistrationService;
import iam.platform.common.dto.request.CreateUserRequest;
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
        model.addAttribute("userRequest", new CreateUserRequest());
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@ModelAttribute @Validated CreateUserRequest request,
            Model model) {
        try {
            registrationService.registerUser(request);
            return "redirect:/bff/login?registered";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            model.addAttribute("userRequest", request);
            return "register";
        }
    }
}
