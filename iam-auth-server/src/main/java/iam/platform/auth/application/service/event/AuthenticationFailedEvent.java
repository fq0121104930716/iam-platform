package iam.platform.auth.application.service.event;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationEvent;
import org.springframework.security.core.AuthenticationException;

import java.time.LocalDateTime;

/**
 * Event published when authentication fails. Used to trigger audit logging for failed login
 * attempts without coupling the failure handler to RocketMQ.
 */
public class AuthenticationFailedEvent extends ApplicationEvent {

    private final String username;
    private final String errorMessage;
    private final HttpServletRequest request;
    private final LocalDateTime timestamp;

    public AuthenticationFailedEvent(Object source, String username, String errorMessage,
            HttpServletRequest request) {
        super(source);
        this.username = username;
        this.errorMessage = errorMessage;
        this.request = request;
        this.timestamp = LocalDateTime.now();
    }

    public static AuthenticationFailedEvent of(Object source, HttpServletRequest request,
            AuthenticationException exception) {
        String username = extractUsername(request);
        String errorMessage = exception != null ? exception.getMessage() : "Unknown error";
        return new AuthenticationFailedEvent(source, username, errorMessage, request);
    }

    private static String extractUsername(HttpServletRequest request) {
        String username = request.getParameter("username");
        return username != null ? username : "unknown";
    }

    public String getUsername() {
        return username;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public LocalDateTime getEventTimestamp() {
        return timestamp;
    }
}
