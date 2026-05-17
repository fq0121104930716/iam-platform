package iam.platform.bff.infrastructure.client;

import iam.platform.bff.infrastructure.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * OpenFeign client for auth service.
 */
@FeignClient(
    name = "iam-auth-service",
    path = "/auth",
    configuration = FeignClientConfig.class
)
public interface AuthFeignClient {

    /**
     * Send SMS verification code.
     */
    @PostMapping("/code/sms")
    ResponseEntity<Void> sendSmsCode(@RequestParam String phone);

    /**
     * Send email verification code.
     */
    @PostMapping("/code/email")
    ResponseEntity<Void> sendEmailCode(@RequestParam String email);
}
