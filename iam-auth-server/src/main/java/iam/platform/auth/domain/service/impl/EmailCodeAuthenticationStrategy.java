package iam.platform.auth.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import iam.platform.auth.domain.model.entity.Person;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;
import iam.platform.auth.domain.model.valueobject.AuthenticationCredentials;
import iam.platform.auth.domain.service.AuthenticationStrategy;
import iam.platform.auth.domain.service.VerificationCodeService;

@Service
@RequiredArgsConstructor
public class EmailCodeAuthenticationStrategy implements AuthenticationStrategy {

    private final VerificationCodeService verificationCodeService;

    @Override
    public AuthenticationMethod getMethod() {
        return AuthenticationMethod.EMAIL_CODE;
    }

    @Override
    public boolean supports(AuthenticationCredentials credentials) {
        return credentials instanceof AuthenticationCredentials.EmailCodeCredentials;
    }

    @Override
    public Person authenticate(AuthenticationCredentials credentials) {
        AuthenticationCredentials.EmailCodeCredentials ec =
                (AuthenticationCredentials.EmailCodeCredentials) credentials;

        if (!verificationCodeService.verifyEmailCode(ec.email(), ec.code())) {
            throw new BadCredentialsException("Invalid or expired email verification code");
        }

        return verificationCodeService.findOrCreatePersonByEmail(ec.email());
    }
}
