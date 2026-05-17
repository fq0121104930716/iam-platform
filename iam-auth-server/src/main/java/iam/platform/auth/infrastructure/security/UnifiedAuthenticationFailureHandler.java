package iam.platform.auth.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

/**
 * Handles authentication failures by redirecting to the login page with an error parameter.
 */
@Slf4j
public class UnifiedAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public UnifiedAuthenticationFailureHandler() {
        super("/login?error");
        setUseForward(false);
        setAllowSessionCreation(true);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) {
        log.debug("Authentication failed: {}", exception.getMessage());
        try {
            super.onAuthenticationFailure(request, response, exception);
        } catch (Exception e) {
            log.error("Failed to handle authentication failure", e);
            try {
                response.sendRedirect("/login?error");
            } catch (Exception ex) {
                log.error("Failed to redirect to login page", ex);
            }
        }
    }
}
