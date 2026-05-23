package iam.platform.auth.application.service.listener;

import iam.platform.auth.application.service.event.AuthenticationCompletedEvent;
import iam.platform.auth.application.service.event.AuthenticationFailedEvent;
import iam.platform.auth.domain.model.entity.User;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;
import iam.platform.common.dto.AuditEventMessage;
import iam.platform.common.model.enums.AuditEventType;
import iam.platform.common.model.enums.AuditResult;
import iam.platform.common.model.enums.EventCategory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Listener: publishes login audit events to RocketMQ. Replaces AuditEventHandler (PostAuthHandler)
 * to eliminate circular dependencies.
 * 
 * Uses ObjectProvider for RocketMQTemplate to gracefully degrade when RocketMQ is unavailable.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "audit.rocketmq.enabled", havingValue = "true", matchIfMissing = true)
public class AuditEventListener {

    private final ObjectProvider<RocketMQTemplate> rocketMQTemplateProvider;
    private static final String AUDIT_TOPIC = "audit-events";
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public AuditEventListener(ObjectProvider<RocketMQTemplate> rocketMQTemplateProvider) {
        this.rocketMQTemplateProvider = rocketMQTemplateProvider;
    }

    @Order(4)
    @EventListener
    public void onAuthenticationCompleted(AuthenticationCompletedEvent event) {
        User user = event.getUser();
        AuthenticationMethod method = event.getMethod();
        HttpServletRequest request = event.getRequest();

        AuditEventMessage auditEvent = AuditEventMessage.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now().format(ISO_FORMATTER))
                .sourceService("iam-auth-service").tenantId(null).userId(user.getId())
                .username(user.getUsername()).eventType(AuditEventType.LOGIN_SUCCESS.name())
                .eventCategory(EventCategory.AUTHENTICATION.name()).resourceType("User")
                .resourceId(user.getId())
                .action("User " + user.getUsername() + " logged in via " + method)
                .ipAddress(extractIpAddress(request)).userAgent(request.getHeader("User-Agent"))
                .requestUri(request.getRequestURI()).result(AuditResult.SUCCESS.name())
                .traceId(extractTraceId(request)).build();

        publishAuditEvent(auditEvent);
    }

    @Order(4)
    @EventListener
    public void onAuthenticationFailed(AuthenticationFailedEvent event) {
        HttpServletRequest request = event.getRequest();

        AuditEventMessage auditEvent = AuditEventMessage.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now().format(ISO_FORMATTER))
                .sourceService("iam-auth-service").tenantId(null).userId(null)
                .username(event.getUsername()).eventType(AuditEventType.LOGIN_FAILURE.name())
                .eventCategory(EventCategory.AUTHENTICATION.name()).resourceType("User")
                .resourceId(null).action("Login failed for user " + event.getUsername())
                .ipAddress(extractIpAddress(request)).userAgent(request.getHeader("User-Agent"))
                .requestUri(request.getRequestURI()).result(AuditResult.FAILURE.name())
                .errorMessage(event.getErrorMessage()).traceId(extractTraceId(request)).build();

        publishAuditEvent(auditEvent);
    }

    private void publishAuditEvent(AuditEventMessage auditEvent) {
        RocketMQTemplate template = rocketMQTemplateProvider.getIfAvailable();
        if (template == null) {
            log.warn(
                    "RocketMQ unavailable, audit event logged locally: eventId={}, type={}, user={}",
                    auditEvent.getEventId(), auditEvent.getEventType(), auditEvent.getUsername());
            return;
        }

        try {
            Message<AuditEventMessage> message = MessageBuilder.withPayload(auditEvent).build();
            template.syncSend(AUDIT_TOPIC + ":AUTHENTICATION", message);
            log.debug("Published audit event: eventId={}, type={}, username={}",
                    auditEvent.getEventId(), auditEvent.getEventType(), auditEvent.getUsername());
        } catch (Exception e) {
            log.error("Failed to publish audit event: eventId={}, username={}",
                    auditEvent.getEventId(), auditEvent.getUsername(), e);
        }
    }

    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractTraceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            traceId = request.getHeader("X-B3-TraceId");
        }
        if (traceId == null || traceId.isEmpty()) {
            traceId = request.getHeader("traceparent");
            if (traceId != null && traceId.contains("-")) {
                String[] parts = traceId.split("-");
                if (parts.length >= 2) {
                    traceId = parts[1];
                }
            }
        }
        return traceId;
    }
}
