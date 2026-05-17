package iam.platform.admin.application.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import iam.platform.admin.application.service.AuditApplicationService;
import iam.platform.admin.domain.model.entity.AuditLog;
import iam.platform.admin.infrastructure.aspect.AuditLogContext;

/**
 * Async event listener that persists audit logs to the database.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogEventListener {

    private final AuditApplicationService auditApplicationService;

    @Async("auditLogExecutor")
    @EventListener
    public void onAuditLogEvent(AuditLogEvent event) {
        AuditLogContext ctx = event.getContext();
        AuditLog auditLog = AuditLog.fromContext(ctx);

        auditApplicationService.saveAuditLog(auditLog);
    }
}
