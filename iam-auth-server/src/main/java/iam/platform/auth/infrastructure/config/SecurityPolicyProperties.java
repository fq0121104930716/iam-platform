package iam.platform.auth.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Security policy configuration properties for rate limiting, account lockout, and IP filtering.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "sso.security")
public class SecurityPolicyProperties {

    private RateLimit rateLimit = new RateLimit();
    private AccountLockout accountLockout = new AccountLockout();
    private List<String> ipWhitelist = new ArrayList<>();
    private List<String> ipBlacklist = new ArrayList<>();

    @Data
    public static class RateLimit {
        private boolean enabled = true;
        private int maxAttempts = 5;
        private int windowSeconds = 300; // 5 minutes
    }

    @Data
    public static class AccountLockout {
        private boolean enabled = true;
        private int threshold = 10;
        private int lockoutDurationMinutes = 30;
    }
}
