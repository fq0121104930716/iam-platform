package iam.platform.auth.application.service.pipeline;

import org.springframework.core.Ordered;

/**
 * Pre-authentication handler interface. All pre-authentication handlers must implement this
 * interface and will be executed in order before the actual authentication strategy runs.
 */
public interface PreAuthHandler extends Ordered {
    /**
     * Handle pre-authentication check.
     *
     * @param context the pre-authentication context
     * @throws PreAuthenticationException if the pre-authentication check fails
     */
    void handle(PreAuthContext context);
}
