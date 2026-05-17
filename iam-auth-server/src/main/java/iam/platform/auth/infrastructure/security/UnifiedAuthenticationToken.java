package iam.platform.auth.infrastructure.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import iam.platform.auth.domain.model.entity.Person;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;
import iam.platform.auth.domain.model.valueobject.AuthenticationCredentials;

import java.util.Collection;

/**
 * Authentication token for the unified authentication flow.
 *
 * Unauthenticated state: holds AuthenticationCredentials (raw input) Authenticated state: holds
 * Person + AuthenticationMethod
 */
public class UnifiedAuthenticationToken extends AbstractAuthenticationToken {

    private final AuthenticationCredentials credentials;
    private final Person person;
    private final AuthenticationMethod method;

    /**
     * Create an unauthenticated token (pre-authentication, holds raw credentials).
     */
    public UnifiedAuthenticationToken(AuthenticationCredentials credentials) {
        super(null);
        this.credentials = credentials;
        this.person = null;
        this.method = null;
        setAuthenticated(false);
    }

    /**
     * Create an authenticated token (post-authentication, holds resolved person).
     */
    public UnifiedAuthenticationToken(Person person, AuthenticationMethod method,
            Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.credentials = null;
        this.person = person;
        this.method = method;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return person != null ? person.getUsername() : null;
    }

    public AuthenticationCredentials getAuthenticationCredentials() {
        return credentials;
    }

    public Person getPerson() {
        return person;
    }

    public AuthenticationMethod getMethod() {
        return method;
    }
}
