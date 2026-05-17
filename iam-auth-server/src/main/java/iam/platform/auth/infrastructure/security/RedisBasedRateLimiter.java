package iam.platform.auth.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import iam.platform.auth.application.service.pipeline.PreAuthenticationException;
import iam.platform.auth.application.service.pipeline.PreAuthContext;
import iam.platform.auth.infrastructure.config.SecurityPolicyProperties;

import java.time.Duration;

/**
 * Redis-based rate limiter for authentication attempts. Uses a sliding window counter approach.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisBasedRateLimiter {

    private final StringRedisTemplate stringRedisTemplate;
    private final SecurityPolicyProperties properties;

    private static final String KEY_PREFIX = "auth:ratelimit:";

    /**
     * Check if the authentication attempt exceeds the rate limit.
     *
     * @param context the pre-authentication context
     * @throws PreAuthenticationException if rate limit is exceeded
     */
    public void checkRateLimit(PreAuthContext context) {
        if (!properties.getRateLimit().isEnabled()) {
            return;
        }

        String identifier = extractIdentifier(context);
        String key = KEY_PREFIX + identifier + ":" + context.getClientIp();

        try {
            String countStr = stringRedisTemplate.opsForValue().get(key);
            int count = countStr != null ? Integer.parseInt(countStr) : 0;

            if (count >= properties.getRateLimit().getMaxAttempts()) {
                long ttl = stringRedisTemplate.getExpire(key);
                if (ttl < 0) {
                    ttl = 0;
                }
                throw new PreAuthenticationException(
                        "Too many authentication attempts. Please try again in " + ttl + " seconds.");
            }

            // Increment counter
            Long newCount = stringRedisTemplate.opsForValue().increment(key);
            if (newCount != null && newCount == 1) {
                // Set TTL on first increment
                stringRedisTemplate.expire(key, Duration.ofSeconds(properties.getRateLimit().getWindowSeconds()));
            }

            log.debug("Rate limit check passed for {}. Attempt count: {}", key, newCount);
        } catch (PreAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            // Redis unavailable - fail open
            log.warn("Redis unavailable for rate limiting, failing open: {}", e.getMessage());
        }
    }

    /**
     * Record a failed authentication attempt.
     */
    public void recordFailure(PreAuthContext context) {
        if (!properties.getRateLimit().isEnabled()) {
            return;
        }

        String identifier = extractIdentifier(context);
        String key = KEY_PREFIX + identifier + ":" + context.getClientIp();

        try {
            Long count = stringRedisTemplate.opsForValue().increment(key);
            if (count != null && count == 1) {
                stringRedisTemplate.expire(key, Duration.ofSeconds(properties.getRateLimit().getWindowSeconds()));
            }
            log.info("Recorded authentication failure for {}. Total: {}", key, count);
        } catch (Exception e) {
            log.warn("Failed to record authentication failure in Redis: {}", e.getMessage());
        }
    }

    /**
     * Record a successful authentication attempt and reset the counter.
     */
    public void recordSuccess(PreAuthContext context) {
        if (!properties.getRateLimit().isEnabled()) {
            return;
        }

        String identifier = extractIdentifier(context);
        String key = KEY_PREFIX + identifier + ":" + context.getClientIp();

        try {
            stringRedisTemplate.delete(key);
            log.debug("Rate limit counter reset for {} after successful authentication", key);
        } catch (Exception e) {
            log.warn("Failed to reset rate limit counter in Redis: {}", e.getMessage());
        }
    }

    /**
     * Extract identifier from credentials for rate limiting.
     * Uses username, phone, or email depending on credential type.
     */
    private String extractIdentifier(PreAuthContext context) {
        // The identifier should be set by CredentialValidationHandler
        if (context.getIdentifierKey() != null) {
            return context.getIdentifierKey();
        }

        // Fallback to IP if no identifier is set
        return context.getClientIp();
    }
}
