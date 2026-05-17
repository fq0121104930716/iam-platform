package iam.platform.admin.application.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import iam.platform.admin.infrastructure.aspect.AuditLogContext;

/**
 * Spring application event for audit log recording.
 */
@Getter
public class AuditLogEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;

    private final transient AuditLogContext context;

    public AuditLogEvent(Object source, AuditLogContext context) {
        super(source);
        this.context = context;
    }
}
