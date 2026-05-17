package iam.platform.auth.domain.service;

import iam.platform.auth.domain.model.entity.Person;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;
import iam.platform.auth.domain.model.valueobject.AuthenticationCredentials;

/**
 * Strategy interface for authentication methods. Each implementation handles credential validation
 * and user resolution for a specific auth type.
 */
public interface AuthenticationStrategy {

    /**
     * The authentication method this strategy handles.
     */
    AuthenticationMethod getMethod();

    /**
     * Check if this strategy supports the given credentials type.
     */
    boolean supports(AuthenticationCredentials credentials);

    /**
     * Validate credentials and return the resolved Person.
     * 
     * @throws org.springframework.security.authentication.BadCredentialsException if validation
     *         fails
     * @throws iam.platform.common.model.exception.InvalidStateException if account state prevents
     *         authentication
     */
    Person authenticate(AuthenticationCredentials credentials);

    /**
     * Returns true if this method involves external redirects (OAuth2 providers). First-party
     * methods (password, SMS, email, LDAP) return false.
     */
    default boolean isRedirectBased() {
        return false;
    }
}
