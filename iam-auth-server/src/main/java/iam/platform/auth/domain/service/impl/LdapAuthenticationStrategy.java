package iam.platform.auth.domain.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import iam.platform.auth.application.service.LdapUserLookupService;
import iam.platform.auth.domain.model.entity.User;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;
import iam.platform.auth.domain.model.valueobject.AuthenticationCredentials;
import iam.platform.auth.domain.service.AuthenticationStrategy;
import iam.platform.auth.infrastructure.config.LdapProperties;

import javax.naming.Context;
import javax.naming.directory.DirContext;
import java.util.Hashtable;

/**
 * Strategy for LDAP/Active Directory authentication.
 *
 * Used by: - Enterprise AD domain login - Corporate directory integration
 *
 * Security considerations: - LDAP connection should use LDAPS (SSL) in production - Password is
 * never stored locally, validated against AD each time - Account lockout policy follows AD
 * configuration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LdapAuthenticationStrategy implements AuthenticationStrategy {

    private final LdapUserLookupService userLookupService;
    private final LdapProperties properties;

    @Override
    public AuthenticationMethod getMethod() {
        return AuthenticationMethod.LDAP;
    }

    @Override
    public boolean supports(AuthenticationCredentials credentials) {
        return credentials instanceof AuthenticationCredentials.LdapCredentials;
    }

    @Override
    public User authenticate(AuthenticationCredentials credentials) {
        if (!(credentials instanceof AuthenticationCredentials.LdapCredentials lc)) {
            throw new IllegalArgumentException("Unsupported credentials type");
        }

        log.debug("Authenticating user with LDAP: username={}, domain={}", lc.username(),
                lc.domain());

        try {
            // 1. Build user DN for bind authentication
            String userDn = buildUserDn(lc.username());

            // 2. Attempt LDAP bind authentication
            authenticateWithLdap(userDn, lc.password());

            // 3. Find or create User in local database
            User user = userLookupService.findOrCreateUserByLdap(lc.username(), lc.domain());

            log.info("LDAP authentication successful: username={}, userId={}", lc.username(),
                    user.getId());

            return user;

        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.error("LDAP authentication failed: username={}", lc.username(), e);
            throw new BadCredentialsException("LDAP authentication failed");
        }
    }

    /**
     * Build the full DN for the user based on configuration.
     */
    private String buildUserDn(String username) {
        // If search filter is configured, use it to find the user DN
        if (properties.getUserSearchFilter() != null && properties.getUserSearchBase() != null) {
            // Try to find user DN by search
            String userDn = userLookupService.findUserDn(username);
            if (userDn != null) {
                return userDn;
            }
        }

        // Fallback: construct DN manually
        if (properties.getUserSearchFilter() != null
                && properties.getUserSearchFilter().contains("sAMAccountName")) {
            // Active Directory format: CN=username,OU=Users,DC=example,DC=com
            return String.format("CN=%s,%s,%s", username, properties.getUserSearchBase(),
                    properties.getBaseDn());
        }

        // Generic LDAP format
        return String.format("uid=%s,%s,%s", username, properties.getUserSearchBase(),
                properties.getBaseDn());
    }

    /**
     * Authenticate with LDAP server using bind method.
     */
    private void authenticateWithLdap(String userDn, String password) {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, properties.getUrls());
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, userDn);
        env.put(Context.SECURITY_CREDENTIALS, password);

        if (properties.isUseSsl()) {
            env.put(Context.SECURITY_PROTOCOL, "ssl");
        }

        try {
            // Attempt to create initial context (this performs the bind)
            DirContext ctx = new javax.naming.directory.InitialDirContext(env);
            ctx.close();
            log.debug("LDAP bind successful for DN: {}", userDn);
        } catch (javax.naming.AuthenticationException e) {
            log.warn("LDAP bind failed for DN: {}: {}", userDn, e.getMessage());
            throw new BadCredentialsException("Invalid LDAP credentials");
        } catch (Exception e) {
            log.error("LDAP connection error: {}", e.getMessage(), e);
            throw new BadCredentialsException("LDAP connection error");
        }
    }

    @Override
    public boolean isRedirectBased() {
        return false;
    }
}
