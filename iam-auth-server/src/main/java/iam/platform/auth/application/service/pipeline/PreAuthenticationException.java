package iam.platform.auth.application.service.pipeline;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception thrown when a pre-authentication check fails.
 */
public class PreAuthenticationException extends AuthenticationException {

    public PreAuthenticationException(String msg) {
        super(msg);
    }

    public PreAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
