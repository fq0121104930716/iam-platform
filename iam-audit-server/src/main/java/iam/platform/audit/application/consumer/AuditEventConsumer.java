package iam.platform.audit.application.consumer;

import iam.platform.audit.domain.model.entity.AuditLog;
import iam.platform.audit.domain.repository.AuditLogRepository;
import iam.platform.common.dto.AuditEventMessage;
import iam.platform.common.model.enums.AuditEventType;
import iam.platform.common.model.enums.AuditResult;
import iam.platform.common.model.enums.EventCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * RocketMQ consumer for audit events.
 * Listens to the audit-events topic and persists messages to the database.
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "audit-events",
        consumerGroup = "audit-server-consumer-group",
        selectorExpression = "*"
)
@RequiredArgsConstructor
public class AuditEventConsumer implements RocketMQListener<AuditEventMessage> {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void onMessage(AuditEventMessage message) {
        log.debug("Received audit event: eventId={}, eventType={}, sourceService={}", 
                message.getEventId(), message.getEventType(), message.getSourceService());

        try {
            // Deduplicate by checking if event already exists
            if (auditLogRepository.findByEventId(message.getEventId()).isPresent()) {
                log.debug("Duplicate audit event, skipping: eventId={}", message.getEventId());
                return;
            }

            // Convert message to domain entity
            AuditLog auditLog = AuditLog.fromEventMessage(
                    message.getEventId(),
                    message.getSourceService(),
                    message.getTenantId(),
                    message.getPersonId(),
                    message.getUsername(),
                    parseEventType(message.getEventType()),
                    parseEventCategory(message.getEventCategory()),
                    message.getResourceId(),
                    message.getResourceType(),
                    message.getAction(),
                    message.getIpAddress(),
                    message.getUserAgent(),
                    message.getRequestUri(),
                    message.getRequestParams(),
                    parseResult(message.getResult()),
                    message.getErrorMessage(),
                    message.getTraceId()
            );

            // Save to database
            auditLogRepository.save(auditLog);
            log.info("Audit log saved: eventId={}, type={}, result={}", 
                    message.getEventId(), message.getEventType(), message.getResult());

        } catch (Exception e) {
            log.error("Failed to process audit event: eventId={}", message.getEventId(), e);
            // Don't rethrow - let RocketMQ handle retry based on configuration
        }
    }

    private AuditEventType parseEventType(String eventType) {
        if (eventType == null || eventType.isEmpty()) {
            return null;
        }
        try {
            return AuditEventType.valueOf(eventType);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown audit event type: {}", eventType);
            return null;
        }
    }

    private EventCategory parseEventCategory(String eventCategory) {
        if (eventCategory == null || eventCategory.isEmpty()) {
            return null;
        }
        try {
            return EventCategory.valueOf(eventCategory);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown audit event category: {}", eventCategory);
            return null;
        }
    }

    private AuditResult parseResult(String result) {
        if (result == null || result.isEmpty()) {
            return AuditResult.SUCCESS;
        }
        try {
            return AuditResult.valueOf(result);
        } catch (IllegalArgumentException e) {
            return AuditResult.SUCCESS;
        }
    }
}
