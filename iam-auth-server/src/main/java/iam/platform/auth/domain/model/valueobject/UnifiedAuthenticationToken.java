package iam.platform.auth.domain.model.valueobject;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import iam.platform.auth.domain.model.entity.User;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;

import java.util.Collection;

/**
 * Authentication token for the unified authentication flow.
 *
 * Unauthenticated state: holds AuthenticationCredentials (raw input) Authenticated state: holds
 * User + AuthenticationMethod
 */
public class UnifiedAuthenticationToken extends AbstractAuthenticationToken {

    private final AuthenticationCredentials credentials;
    private final User user;
    private final AuthenticationMethod method;

    /**
     * Create an unauthenticated token (pre-authentication, holds raw credentials).
     */
    public UnifiedAuthenticationToken(AuthenticationCredentials credentials) {
        super(null);
        this.credentials = credentials;
        this.user = null;
        this.method = null;
        setAuthenticated(false);
    }

    /**
     * Create an authenticated token (post-authentication, holds resolved User).
     */
    public UnifiedAuthenticationToken(User user, AuthenticationMethod method,
            Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.credentials = null;
        this.user = user;
        this.method = method;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return user != null ? user.getUsername() : null;
    }

    public AuthenticationCredentials getAuthenticationCredentials() {
        return credentials;
    }

    public User getUser() {
        return user;
    }

    public AuthenticationMethod getMethod() {
        return method;
    }
}
