package iam.platform.auth.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import iam.platform.auth.domain.model.valueobject.AuthenticationCredentials;

/**
 * Unified authentication filter that handles all first-party authentication methods (password, SMS
 * code, email code, LDAP) through a single POST endpoint (/login).
 *
 * Reads a hidden 'method' field to determine the authentication type and extracts the appropriate
 * credentials from the form submission.
 */
public class UnifiedAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String PARAM_METHOD = "method";
    private static final String PARAM_USERNAME = "username";
    private static final String PARAM_PASSWORD = "password";
    private static final String PARAM_PHONE = "phone";
    private static final String PARAM_EMAIL = "email";
    private static final String PARAM_CODE = "code";

    public UnifiedAuthenticationFilter(String loginProcessingUrl) {
        super(new AntPathRequestMatcher(loginProcessingUrl, "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException {
        AuthenticationCredentials credentials = parseCredentials(request);
        UnifiedAuthenticationToken authRequest = new UnifiedAuthenticationToken(credentials);
        setDetails(request, authRequest);
        return getAuthenticationManager().authenticate(authRequest);
    }

    /**
     * Parse the form submission into the appropriate AuthenticationCredentials variant.
     */
    private AuthenticationCredentials parseCredentials(HttpServletRequest request) {
        String method = request.getParameter(PARAM_METHOD);
        if (method == null)
            method = "password"; // default to password

        return switch (method.toLowerCase()) {
            case "password" -> new AuthenticationCredentials.PasswordCredentials(
                    getRequiredParam(request, PARAM_USERNAME),
                    getRequiredParam(request, PARAM_PASSWORD));
            case "sms" -> new AuthenticationCredentials.SmsCodeCredentials(
                    getRequiredParam(request, PARAM_PHONE), getRequiredParam(request, PARAM_CODE));
            case "email" -> new AuthenticationCredentials.EmailCodeCredentials(
                    getRequiredParam(request, PARAM_EMAIL), getRequiredParam(request, PARAM_CODE));
            case "ldap" -> new AuthenticationCredentials.LdapCredentials(
                    getRequiredParam(request, PARAM_USERNAME),
                    getRequiredParam(request, PARAM_PASSWORD), request.getParameter("domain"));
            default -> throw new org.springframework.security.authentication.BadCredentialsException(
                    "Unknown authentication method: " + method);
        };
    }

    private String getRequiredParam(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.isBlank()) {
            throw new org.springframework.security.authentication.BadCredentialsException(
                    "Missing required parameter: " + name);
        }
        return value;
    }

    /**
     * Set authentication details (remote address, session ID, etc.) on the token.
     */
    protected void setDetails(HttpServletRequest request, UnifiedAuthenticationToken authRequest) {
        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
    }
}
