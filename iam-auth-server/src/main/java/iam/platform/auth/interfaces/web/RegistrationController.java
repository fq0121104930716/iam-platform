package iam.platform.auth.interfaces.web;

import iam.platform.common.dto.request.CreateUserCredentialRequest;
import iam.platform.common.dto.response.UserCredentialResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import iam.platform.common.dto.request.CreateUserRequest;
import iam.platform.auth.interfaces.client.AdminServiceClient;
import iam.platform.common.model.exception.ConflictException;

import java.net.URI;

@Slf4j
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

        // Validate password is provided for registration
        // TODO: Refactor to collect password separately from user creation
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            model.addAttribute("error", "Password is required for registration");
            return "register";
        }

        try {
            // Step 1: Create user account
            ResponseEntity<Void> createResponse = adminServiceClient.createUser(request);

            // Step 2: Extract userId from Location header
            Long userId = extractUserIdFromLocation(createResponse.getHeaders().getLocation());
            if (userId == null) {
                log.error("Failed to extract userId from registration response");
                model.addAttribute("error", "Registration failed. Please try again later.");
                return "register";
            }

            // Step 3: Create initial password credential
            CreateUserCredentialRequest credentialRequest =
                    CreateUserCredentialRequest.builder().credentialType("PASSWORD")
                            .credentialValue(request.getPassword()).isPrimary(true)
                            .description("Initial password set during registration").build();

            ResponseEntity<UserCredentialResponse> credentialResponse =
                    adminServiceClient.createCredential(userId, credentialRequest);

            if (credentialResponse.getStatusCode().is2xxSuccessful()) {
                log.info("User registered successfully with initial password credential: userId={}",
                        userId);
                return "redirect:/login?registered";
            } else {
                log.error("Failed to create initial password credential for userId={}", userId);
                model.addAttribute("error",
                        "Registration partially failed. Account created but password not set. Please contact support.");
                return "register";
            }
        } catch (ConflictException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        } catch (Exception e) {
            log.error("Registration failed", e);
            model.addAttribute("error", "Registration failed. Please try again later.");
            return "register";
        }
    }

    /**
     * Extract userId from the Location header URI. Expected format: http://host/v1/users/{userId}
     */
    private Long extractUserIdFromLocation(URI location) {
        if (location == null) {
            return null;
        }

        try {
            String path = location.getPath();
            // Extract the last path segment which should be the userId
            String[] segments = path.split("/");
            if (segments.length > 0) {
                return Long.parseLong(segments[segments.length - 1]);
            }
        } catch (NumberFormatException e) {
            log.warn("Failed to parse userId from location: {}", location, e);
        }

        return null;
    }
}
