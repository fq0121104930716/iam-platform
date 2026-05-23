package iam.platform.auth.application.service.event;

import iam.platform.auth.domain.model.entity.User;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;
import iam.platform.auth.domain.model.valueobject.AuthenticationResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Event published when authentication is completed successfully. Replaces the synchronous
 * PostAuthenticationPipeline to eliminate circular dependencies.
 * 
 * This event is published by AuthenticationApplicationService after the core authentication logic
 * completes. Since Spring's publishEvent() is synchronous by default, listeners execute before the
 * publish call returns. Listeners can write back results via the embedded ResultHolder.
 */
public class AuthenticationCompletedEvent extends ApplicationEvent {

    private final User user;
    private final AuthenticationMethod method;
    private final HttpServletRequest request;
    private final LocalDateTime timestamp;
    private final ResultHolder resultHolder = new ResultHolder();

    public AuthenticationCompletedEvent(Object source, User user, AuthenticationMethod method,
            HttpServletRequest request, LocalDateTime timestamp) {
        super(source);
        this.user = user;
        this.method = method;
        this.request = request;
        this.timestamp = timestamp;
    }

    public static AuthenticationCompletedEvent of(Object source, User user,
            AuthenticationMethod method, HttpServletRequest request) {
        return new AuthenticationCompletedEvent(source, user, method, request, LocalDateTime.now());
    }

    public User getUser() {
        return user;
    }

    public AuthenticationMethod getMethod() {
        return method;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public LocalDateTime getEventTimestamp() {
        return timestamp;
    }

    public ResultHolder getResultHolder() {
        return resultHolder;
    }

    /**
     * Mutable holder for passing AuthenticationResult back from event listeners to the publisher.
     * This works because Spring's default event publishing is synchronous.
     */
    public static class ResultHolder {
        private AuthenticationResult result;

        public void setResult(AuthenticationResult result) {
            this.result = result;
        }

        public AuthenticationResult getResult() {
            return result;
        }
    }
}
