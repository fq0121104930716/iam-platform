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
 * Account lockout manager using Redis. Tracks failed authentication attempts and locks accounts
 * when the threshold is exceeded.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountLockoutManager {

    private final StringRedisTemplate stringRedisTemplate;
    private final SecurityPolicyProperties properties;

    private static final String LOCKOUT_KEY_PREFIX = "auth:lockout:";
    private static final String FAILURE_COUNT_KEY_PREFIX = "auth:failurecount:";

    /**
     * Check if the account is currently locked out.
     *
     * @param context the pre-authentication context
     * @throws PreAuthenticationException if account is locked
     */
    public void checkAccountLockout(PreAuthContext context) {
        if (!properties.getAccountLockout().isEnabled()) {
            return;
        }

        String identifier = extractIdentifier(context);
        String lockoutKey = LOCKOUT_KEY_PREFIX + identifier;

        try {
            Boolean isLocked = stringRedisTemplate.hasKey(lockoutKey);
            if (Boolean.TRUE.equals(isLocked)) {
                long ttl = stringRedisTemplate.getExpire(lockoutKey);
                if (ttl < 0) {
                    ttl = 0;
                }
                throw new PreAuthenticationException(
                        "Account is temporarily locked. Please try again in " + ttl + " seconds.");
            }
        } catch (PreAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            // Redis unavailable - fail open
            log.warn("Redis unavailable for account lockout check, failing open: {}",
                    e.getMessage());
        }
    }

    /**
     * Record a failed authentication attempt and potentially lock the account.
     */
    public void recordFailure(PreAuthContext context) {
        if (!properties.getAccountLockout().isEnabled()) {
            return;
        }

        String identifier = extractIdentifier(context);
        String failureCountKey = FAILURE_COUNT_KEY_PREFIX + identifier;
        String lockoutKey = LOCKOUT_KEY_PREFIX + identifier;

        try {
            // Increment failure count
            Long failureCount = stringRedisTemplate.opsForValue().increment(failureCountKey);
            if (failureCount != null && failureCount == 1) {
                // Set TTL to window (same as rate limit window)
                stringRedisTemplate.expire(failureCountKey,
                        Duration.ofSeconds(properties.getRateLimit().getWindowSeconds()));
            }

            // Check if threshold exceeded
            if (failureCount != null
                    && failureCount >= properties.getAccountLockout().getThreshold()) {
                // Lock the account
                stringRedisTemplate.opsForValue().set(lockoutKey, "locked", Duration
                        .ofMinutes(properties.getAccountLockout().getLockoutDurationMinutes()));
                log.warn("Account locked due to too many failed attempts: {}", identifier);

                // Reset failure count
                stringRedisTemplate.delete(failureCountKey);
            }

            log.info("Recorded authentication failure for {}. Total failures: {}", identifier,
                    failureCount);
        } catch (Exception e) {
            log.warn("Failed to record account lockout failure in Redis: {}", e.getMessage());
        }
    }

    /**
     * Record a successful authentication attempt and reset counters.
     */
    public void recordSuccess(PreAuthContext context) {
        if (!properties.getAccountLockout().isEnabled()) {
            return;
        }

        String identifier = extractIdentifier(context);
        String failureCountKey = FAILURE_COUNT_KEY_PREFIX + identifier;
        String lockoutKey = LOCKOUT_KEY_PREFIX + identifier;

        try {
            // Reset failure count
            stringRedisTemplate.delete(failureCountKey);
            // Remove lockout if present
            stringRedisTemplate.delete(lockoutKey);
            log.debug("Account lockout counters reset for {} after successful authentication",
                    identifier);
        } catch (Exception e) {
            log.warn("Failed to reset account lockout counters in Redis: {}", e.getMessage());
        }
    }

    /**
     * Extract identifier from context.
     */
    private String extractIdentifier(PreAuthContext context) {
        if (context.getIdentifierKey() != null) {
            return context.getIdentifierKey();
        }
        return context.getClientIp();
    }
}
