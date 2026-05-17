package iam.platform.auth.application.service.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import iam.platform.auth.domain.model.entity.Person;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;

/**
 * Pipeline handler: publishes login audit event. This is a placeholder - actual audit event
 * publishing will be integrated with the audit system.
 */
@Slf4j
@Component
public class AuditEventHandler implements PostAuthHandler {

    private final ApplicationEventPublisher eventPublisher;

    public AuditEventHandler(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void handle(PostAuthContext context) {
        Person person = context.getPerson();
        AuthenticationMethod method = context.getMethod();

        // TODO: Integrate with audit event system
        // For now, just log the successful authentication
        log.debug("Login audit: person={}, method={}, time={}", person.getUsername(), method,
                context.toResult().authenticatedAt());

        // Future: publish audit event
        // eventPublisher.publishEvent(new LoginSuccessAuditEvent(...));
    }

    @Override
    public int getOrder() {
        return 600;
    }
}
