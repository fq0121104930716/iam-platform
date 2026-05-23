package iam.platform.auth.application.service.listener;

import iam.platform.auth.application.service.event.AuthenticationCompletedEvent;
import iam.platform.auth.domain.service.AccountStatusPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Listener: validates User status after authentication. Replaces AccountStatusValidationHandler
 * (PostAuthHandler) to eliminate circular dependencies.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountStatusValidationListener {

    private final AccountStatusPolicy accountStatusPolicy;

    @Order(1)
    @EventListener
    public void onAuthenticationCompleted(AuthenticationCompletedEvent event) {
        log.debug("Validating account status for user: {}", event.getUser().getUsername());
        accountStatusPolicy.validateUserStatus(event.getUser());
    }
}
