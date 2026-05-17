package iam.platform.admin.infrastructure.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import iam.platform.common.model.annotation.AuditLog;
import iam.platform.common.model.enums.AuditResult;
import iam.platform.admin.infrastructure.config.AuditProperties;

/**
 * AOP aspect that intercepts methods annotated with @AuditLog and records audit events.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogAspect {

    private final ApplicationEventPublisher eventPublisher;
    private final AuditLogContextBuilder contextBuilder;
    private final AuditProperties auditProperties;

    @Around("@annotation(auditLogAnnotation)")
    public Object auditMethodExecution(ProceedingJoinPoint joinPoint, AuditLog auditLogAnnotation)
            throws Throwable {
        if (!auditProperties.isEnabled()) {
            return joinPoint.proceed();
        }

        AuditLogContext context =
                contextBuilder.build(joinPoint, auditLogAnnotation, joinPoint.getArgs());

        try {
            Object result = joinPoint.proceed();
            context.setResult(AuditResult.SUCCESS);
            return result;
        } catch (Exception ex) {
            context.setResult(AuditResult.FAILURE);
            context.setErrorMessage(truncate(ex.getMessage(), 2000));
            throw ex;
        } finally {
            publishAuditEvent(context);
        }
    }

    private void publishAuditEvent(AuditLogContext context) {
        try {
            iam.platform.admin.application.event.AuditLogEvent event =
                    new iam.platform.admin.application.event.AuditLogEvent(this, context);
            eventPublisher.publishEvent(event);
        } catch (Exception e) {
            log.error("Failed to publish audit event: {}", e.getMessage(), e);
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null)
            return null;
        return str.length() <= maxLength ? str : str.substring(0, maxLength);
    }
}
