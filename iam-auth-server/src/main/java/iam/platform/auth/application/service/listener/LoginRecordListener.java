package iam.platform.auth.application.service.listener;

import iam.platform.auth.application.service.event.AuthenticationCompletedEvent;
import iam.platform.auth.domain.model.entity.User;
import iam.platform.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Listener: records the successful login timestamp. Replaces LoginRecordHandler (PostAuthHandler)
 * to eliminate circular dependencies.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginRecordListener {

    private final UserRepository userRepository;

    @Order(3)
    @EventListener
    public void onAuthenticationCompleted(AuthenticationCompletedEvent event) {
        User user = event.getUser();
        log.debug("Recording successful login for user: {}", user.getUsername());

        User updatedUser = user.toBuilder().lastLoginAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now()).build();
        userRepository.save(updatedUser);
    }
}
