package iam.platform.auth.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import iam.platform.auth.domain.model.entity.User;
import iam.platform.auth.domain.model.valueobject.AuthenticationCredentials;

import java.util.List;

/**
 * Authentication dispatcher that routes authentication requests to the appropriate strategy.
 * 
 * This is the central coordination point for all authentication methods: - PASSWORD (username +
 * password) - SMS_CODE (phone + verification code) - EMAIL_CODE (email + verification code) -
 * OAUTH2_* (social login via third-party providers) - LDAP (enterprise directory service)
 * 
 * Future extensions: - SAML (enterprise SSO protocol) - CAS (Central Authentication Service) -
 * Biometric authentication - Hardware token (TOTP, FIDO2)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationDispatcher {

    private final List<AuthenticationStrategy> strategies;

    /**
     * Authenticate using the provided credentials.
     * 
     * @param credentials The authentication credentials
     * @return The authenticated User entity
     * @throws BadCredentialsException if authentication fails
     */
    public User authenticate(AuthenticationCredentials credentials) {
        AuthenticationStrategy strategy = findStrategy(credentials);

        log.debug("Dispatching authentication to strategy: {}",
                strategy.getClass().getSimpleName());

        return strategy.authenticate(credentials);
    }

    /**
     * Find the appropriate strategy for the given credentials.
     */
    private AuthenticationStrategy findStrategy(AuthenticationCredentials credentials) {
        return strategies.stream().filter(s -> s.supports(credentials)).findFirst()
                .orElseThrow(() -> new BadCredentialsException(
                        "No authentication strategy found for credentials type: "
                                + credentials.getClass().getSimpleName()));
    }
}
