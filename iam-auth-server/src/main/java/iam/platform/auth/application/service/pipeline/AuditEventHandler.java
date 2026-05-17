package iam.platform.auth.application.service.pipeline;

import iam.platform.common.dto.AuditEventMessage;
import iam.platform.common.model.enums.AuditEventType;
import iam.platform.common.model.enums.AuditResult;
import iam.platform.common.model.enums.EventCategory;
import iam.platform.auth.domain.model.entity.Person;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Pipeline handler: publishes login audit event to RocketMQ.
 */
@Slf4j
@Component
public class AuditEventHandler implements PostAuthHandler {

    private final RocketMQTemplate rocketMQTemplate;

    private static final String AUDIT_TOPIC = "audit-events";
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public AuditEventHandler(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @Override
    public void handle(PostAuthContext context) {
        Person person = context.getPerson();
        AuthenticationMethod method = context.getMethod();

        try {
            // Extract info from request
            String ipAddress = extractIpAddress(context.getRequest());
            String userAgent = context.getRequest().getHeader("User-Agent");
            String requestUri = context.getRequest().getRequestURI();
            Long tenantId = context.getSelectedTenantAccount() != null 
                    ? context.getSelectedTenantAccount().getTenantId() 
                    : null;

            // Build audit event message
            AuditEventMessage auditEvent = AuditEventMessage.builder()
                    .eventId(UUID.randomUUID().toString())
                    .timestamp(LocalDateTime.now().format(ISO_FORMATTER))
                    .sourceService("iam-auth-service")
                    .tenantId(tenantId)
                    .personId(person.getId())
                    .username(person.getUsername())
                    .eventType(AuditEventType.LOGIN_SUCCESS.name())
                    .eventCategory(EventCategory.AUTHENTICATION.name())
                    .resourceType("person")
                    .resourceId(person.getId())
                    .action("User " + person.getUsername() + " logged in via " + method)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .requestUri(requestUri)
                    .result(AuditResult.SUCCESS.name())
                    .build();

            // Send to RocketMQ
            Message<AuditEventMessage> message = MessageBuilder.withPayload(auditEvent).build();
            rocketMQTemplate.syncSend(AUDIT_TOPIC + ":AUTHENTICATION", message);

            log.debug("Published login audit event: eventId={}, username={}, method={}", 
                    auditEvent.getEventId(), person.getUsername(), method);

        } catch (Exception e) {
            log.error("Failed to publish login audit event for user: {}", person.getUsername(), e);
        }
    }

    private String extractIpAddress(jakarta.servlet.http.HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    public int getOrder() {
        return 600;
    }
}
