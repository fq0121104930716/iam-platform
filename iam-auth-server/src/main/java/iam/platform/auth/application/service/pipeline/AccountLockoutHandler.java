package iam.platform.auth.application.service.pipeline;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import iam.platform.auth.infrastructure.security.AccountLockoutManager;

/**
 * Account lockout handler that temporarily locks accounts after too many failed attempts.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountLockoutHandler implements PreAuthHandler {

    private final AccountLockoutManager lockoutManager;

    @Override
    public void handle(PreAuthContext context) {
        lockoutManager.checkAccountLockout(context);
    }

    @Override
    public int getOrder() {
        return 20;
    }

    /**
     * Record a failed authentication attempt.
     */
    public void recordFailure(PreAuthContext context) {
        lockoutManager.recordFailure(context);
    }

    /**
     * Record a successful authentication attempt.
     */
    public void recordSuccess(PreAuthContext context) {
        lockoutManager.recordSuccess(context);
    }
}
