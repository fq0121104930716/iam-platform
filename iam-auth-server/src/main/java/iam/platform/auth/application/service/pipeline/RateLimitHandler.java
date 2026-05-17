package iam.platform.auth.application.service.pipeline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import iam.platform.auth.infrastructure.security.RedisBasedRateLimiter;

/**
 * Rate limit handler that prevents brute force attacks by limiting authentication attempts.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitHandler implements PreAuthHandler {

    private final RedisBasedRateLimiter rateLimiter;

    @Override
    public void handle(PreAuthContext context) {
        rateLimiter.checkRateLimit(context);
    }

    @Override
    public int getOrder() {
        return 10;
    }

    /**
     * Record a failed authentication attempt.
     */
    public void recordFailure(PreAuthContext context) {
        rateLimiter.recordFailure(context);
    }

    /**
     * Record a successful authentication attempt.
     */
    public void recordSuccess(PreAuthContext context) {
        rateLimiter.recordSuccess(context);
    }
}
