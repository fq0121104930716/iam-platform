package iam.platform.auth.domain.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import iam.platform.auth.domain.model.entity.User;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;
import iam.platform.auth.domain.model.valueobject.AuthenticationCredentials;
import iam.platform.auth.domain.repository.UserRepository;
import iam.platform.auth.domain.repository.UserCredentialRepository;
import iam.platform.auth.domain.service.AuthenticationStrategy;
import iam.platform.common.model.enums.CredentialType;

/**
 * Strategy for password-based authentication (username + password).
 * 
 * Used by:
 * - OAuth2 Password Grant (for legacy third-party applications)
 * - Traditional form-based login
 * 
 * Security considerations:
 * - Passwords are stored using BCrypt in t_user_credential table
 * - This strategy should only be enabled for high-trust clients
 * - OAuth2 spec recommends authorization_code + PKCE instead
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordAuthenticationStrategy implements AuthenticationStrategy {

    private final UserRepository userRepository;
    private final UserCredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthenticationMethod getMethod() {
        return AuthenticationMethod.PASSWORD;
    }

    @Override
    public boolean supports(AuthenticationCredentials credentials) {
        return credentials instanceof AuthenticationCredentials.PasswordCredentials;
    }

    @Override
    public User authenticate(AuthenticationCredentials credentials) {
        if (!(credentials instanceof AuthenticationCredentials.PasswordCredentials pc)) {
            throw new IllegalArgumentException("Unsupported credentials type");
        }

        log.debug("Authenticating user with password: username={}", pc.username());

        // Find User by username
        User user = userRepository.findByUsername(pc.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        // Get password credential from credential table
        var credentialInfo = credentialRepository.findPrimaryCredential(user.getId(), CredentialType.PASSWORD)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        // Validate password
        if (!passwordEncoder.matches(pc.password(), credentialInfo.credentialValue())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        // Update last used timestamp
        credentialRepository.updateLastUsed(credentialInfo.id());

        log.info("Password authentication successful: username={}, userId={}", pc.username(),
                user.getId());

        return user;
    }
}
