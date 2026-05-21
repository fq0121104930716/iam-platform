package iam.platform.auth.domain.model.valueobject;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import iam.platform.auth.domain.service.context.TenantContext;

/**
 * Represents an authentication token that includes tenant context information. This token extends
 * the standard Spring Security authentication with tenant-specific data for multi-tenant SSO
 * scenarios.
 */
public class TenantAwareAuthenticationToken implements Authentication {
    private static final long serialVersionUID = 1L;

    private final transient Object principal;
    private final transient Object credentials;
    private final Long tenantId;
    private final String tenantCode;
    private final transient java.util.Set<String> permissions;

    private boolean authenticated = false;
    private transient Authentication originalAuthentication;
    private transient Object details;

    /**
     * Creates an unauthenticated token (used during login request).
     */
    public TenantAwareAuthenticationToken(Object principal, Object credentials) {
        this.principal = principal;
        this.credentials = credentials;
        this.tenantId = null;
        this.tenantCode = null;
        this.permissions = java.util.Collections.emptySet();
    }

    /**
     * Creates an authenticated token with full tenant context.
     */
    public TenantAwareAuthenticationToken(Object principal, Long tenantId, String tenantCode,
            java.util.Set<String> permissions) {
        this.principal = principal;
        this.credentials = null;
        this.tenantId = tenantId;
        this.tenantCode = tenantCode;
        this.permissions = permissions != null ? permissions : java.util.Collections.emptySet();
    }

    /**
     * Creates an authenticated token from an existing authentication, adding tenant context.
     */
    public static TenantAwareAuthenticationToken fromAuthentication(Authentication authentication,
            Long tenantId, String tenantCode, java.util.Set<String> permissions) {
        TenantAwareAuthenticationToken token = new TenantAwareAuthenticationToken(
                authentication.getPrincipal(), tenantId, tenantCode, permissions);
        token.setAuthenticated(true);
        token.details = authentication.getDetails();
        token.originalAuthentication = authentication;
        return token;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public java.util.Set<String> getPermissions() {
        return permissions;
    }

    public Authentication getOriginalAuthentication() {
        return originalAuthentication;
    }

    @Override
    public String getName() {
        return principal != null ? principal.toString() : "";
    }

    @Override
    public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
        if (originalAuthentication != null) {
            return originalAuthentication.getAuthorities();
        }
        return java.util.Collections.emptyList();
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getDetails() {
        return details;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean authenticated) throws IllegalArgumentException {
        this.authenticated = authenticated;
    }

    /**
     * Store this token in the SecurityContext and update TenantContext.
     */
    public void registerToContext() {
        SecurityContextHolder.getContext().setAuthentication(this);
        if (tenantId != null) {
            TenantContext.setCurrentTenantId(tenantId);
        }
    }

    /**
     * Clear the SecurityContext and TenantContext.
     */
    public static void clearContext() {
        SecurityContextHolder.clearContext();
        TenantContext.clear();
    }
}
