package iam.platform.auth.domain.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import iam.platform.auth.domain.model.entity.Person;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;
import iam.platform.auth.domain.model.valueobject.AuthenticationCredentials;
import iam.platform.auth.domain.repository.PersonRepository;
import iam.platform.auth.domain.service.AuthenticationStrategy;

/**
 * Strategy for password-based authentication (username + password).
 * 
 * Used by: - OAuth2 Password Grant (for legacy third-party applications) - Traditional form-based
 * login
 * 
 * Security considerations: - Passwords are stored using BCrypt - This strategy should only be
 * enabled for high-trust clients - OAuth2 spec recommends authorization_code + PKCE instead
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordAuthenticationStrategy implements AuthenticationStrategy {

    private final PersonRepository personRepository;
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
    public Person authenticate(AuthenticationCredentials credentials) {
        if (!(credentials instanceof AuthenticationCredentials.PasswordCredentials pc)) {
            throw new IllegalArgumentException("Unsupported credentials type");
        }

        log.debug("Authenticating user with password: username={}", pc.username());

        // Find person by username
        Person person = personRepository.findByUsername(pc.username())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        // Validate password
        if (!passwordEncoder.matches(pc.password(), person.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        log.info("Password authentication successful: username={}, personId={}", pc.username(),
                person.getId());

        return person;
    }
}
