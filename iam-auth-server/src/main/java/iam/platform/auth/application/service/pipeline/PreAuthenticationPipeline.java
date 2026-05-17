package iam.platform.auth.application.service.pipeline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Pre-authentication pipeline that executes all PreAuthHandler beans in order before the actual
 * authentication strategy runs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreAuthenticationPipeline {

    private final List<PreAuthHandler> handlers;

    /**
     * Execute all pre-authentication checks in order.
     *
     * @param context the pre-authentication context
     * @throws PreAuthenticationException if any pre-authentication check fails
     */
    public void execute(PreAuthContext context) {
        for (PreAuthHandler handler : handlers) {
            log.debug("Executing pre-auth handler: {}", handler.getClass().getSimpleName());
            handler.handle(context);
        }
    }

    /**
     * Record a failed authentication attempt for rate limiting and lockout tracking.
     */
    public void recordFailure(PreAuthContext context) {
        // Find handlers that need to record failures
        for (PreAuthHandler handler : handlers) {
            if (handler instanceof RateLimitHandler rateLimitHandler) {
                rateLimitHandler.recordFailure(context);
            } else if (handler instanceof AccountLockoutHandler lockoutHandler) {
                lockoutHandler.recordFailure(context);
            }
        }
    }

    /**
     * Record a successful authentication attempt to reset counters.
     */
    public void recordSuccess(PreAuthContext context) {
        // Find handlers that need to record successes
        for (PreAuthHandler handler : handlers) {
            if (handler instanceof RateLimitHandler rateLimitHandler) {
                rateLimitHandler.recordSuccess(context);
            } else if (handler instanceof AccountLockoutHandler lockoutHandler) {
                lockoutHandler.recordSuccess(context);
            }
        }
    }
}
