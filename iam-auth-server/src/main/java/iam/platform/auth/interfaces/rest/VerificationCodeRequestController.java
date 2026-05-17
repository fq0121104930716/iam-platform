package iam.platform.auth.interfaces.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import iam.platform.auth.domain.service.VerificationCodeService;
import iam.platform.common.api.ApiResponse;

/**
 * REST controller for sending verification codes. Authentication via verification code is handled
 * by the unified login form POST /login.
 */
@RestController
@RequestMapping("/auth/code")
@RequiredArgsConstructor
public class VerificationCodeRequestController {

    private final VerificationCodeService verificationCodeService;

    @PostMapping("/sms")
    public ApiResponse<Void> requestSmsCode(@RequestParam String phone) {
        verificationCodeService.sendSmsCode(phone);
        return ApiResponse.success(null);
    }

    @PostMapping("/email")
    public ApiResponse<Void> requestEmailCode(@RequestParam String email) {
        verificationCodeService.sendEmailCode(email);
        return ApiResponse.success(null);
    }
}
