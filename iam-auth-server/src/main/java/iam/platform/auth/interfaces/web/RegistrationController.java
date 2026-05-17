package iam.platform.auth.interfaces.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import iam.platform.common.dto.request.CreateUserRequest;
import iam.platform.auth.interfaces.client.AdminServiceClient;
import iam.platform.common.model.exception.ConflictException;

@Controller
@RequiredArgsConstructor
public class RegistrationController {

    private final AdminServiceClient adminServiceClient;

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", CreateUserRequest.builder().build());
        return "register";
    }

    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("user") CreateUserRequest request,
            BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            adminServiceClient.createUser(request);
            return "redirect:/login?registered";
        } catch (ConflictException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed. Please try again later.");
            return "register";
        }
    }
}
