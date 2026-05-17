package iam.platform.auth.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import iam.platform.auth.domain.model.entity.User;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;
import iam.platform.auth.domain.model.valueobject.AuthenticationCredentials;
import iam.platform.auth.domain.service.AuthenticationStrategy;
import iam.platform.auth.domain.service.VerificationCodeService;

@Service
@RequiredArgsConstructor
public class SmsCodeAuthenticationStrategy implements AuthenticationStrategy {

    private final VerificationCodeService verificationCodeService;

    @Override
    public AuthenticationMethod getMethod() {
        return AuthenticationMethod.SMS_CODE;
    }

    @Override
    public boolean supports(AuthenticationCredentials credentials) {
        return credentials instanceof AuthenticationCredentials.SmsCodeCredentials;
    }

    @Override
    public User authenticate(AuthenticationCredentials credentials) {
        AuthenticationCredentials.SmsCodeCredentials sc =
                (AuthenticationCredentials.SmsCodeCredentials) credentials;

        if (!verificationCodeService.verifySmsCode(sc.phone(), sc.code())) {
            throw new BadCredentialsException("Invalid or expired SMS verification code");
        }

        return verificationCodeService.findOrCreateUserByPhone(sc.phone());
    }
}
