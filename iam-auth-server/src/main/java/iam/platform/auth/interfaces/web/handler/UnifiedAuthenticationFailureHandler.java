package iam.platform.auth.interfaces.web.handler;

import iam.platform.auth.application.service.event.AuthenticationFailedEvent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

/**
 * Handles authentication failures by redirecting to the login page with an error parameter.
 * Publishes AuthenticationFailedEvent for audit logging.
 */
@Slf4j
public class UnifiedAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final ApplicationEventPublisher eventPublisher;

    public UnifiedAuthenticationFailureHandler(ApplicationEventPublisher eventPublisher) {
        super("/login?error");
        setUseForward(false);
        setAllowSessionCreation(true);
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) {
        log.debug("Authentication failed: {}", exception.getMessage());

        // Publish failure event for audit logging
        try {
            eventPublisher.publishEvent(AuthenticationFailedEvent.of(this, request, exception));
        } catch (Exception e) {
            log.warn("Failed to publish authentication failure event", e);
        }

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
