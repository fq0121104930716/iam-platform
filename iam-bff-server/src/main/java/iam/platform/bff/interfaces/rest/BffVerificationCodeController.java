package iam.platform.bff.interfaces.rest;

import iam.platform.bff.infrastructure.client.AuthFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API controller for BFF verification code endpoints.
 * Forwards requests to auth-service via Feign client.
 */
@RestController
@RequestMapping("/bff/api/code")
@RequiredArgsConstructor
@Slf4j
public class BffVerificationCodeController {

    private final AuthFeignClient authFeignClient;

    /**
     * Send SMS verification code.
     */
    @PostMapping("/sms")
    public ResponseEntity<Void> sendSmsCode(@RequestParam String phone) {
        log.info("Sending SMS verification code to: {}", phone);
        return authFeignClient.sendSmsCode(phone);
    }

    /**
     * Send email verification code.
     */
    @PostMapping("/email")
    public ResponseEntity<Void> sendEmailCode(@RequestParam String email) {
        log.info("Sending email verification code to: {}", email);
        return authFeignClient.sendEmailCode(email);
    }
}
