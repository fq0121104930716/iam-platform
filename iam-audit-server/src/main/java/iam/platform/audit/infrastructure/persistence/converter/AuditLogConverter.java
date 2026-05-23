package iam.platform.audit.infrastructure.persistence.converter;

import iam.platform.audit.domain.model.entity.AuditLog;
import iam.platform.audit.infrastructure.persistence.entity.AuditLogPO;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Converter between AuditLog domain entity and AuditLogPO persistence entity.
 */
@Component
public class AuditLogConverter {

    public AuditLogPO toPO(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }

        return AuditLogPO.builder().id(auditLog.getId()).eventId(auditLog.getEventId())
                .sourceService(auditLog.getSourceService()).tenantId(auditLog.getTenantId())
                .userId(auditLog.getUserId()).username(auditLog.getUsername())
                .eventType(auditLog.getEventType()).eventCategory(auditLog.getEventCategory())
                .resourceId(auditLog.getResourceId()).resourceType(auditLog.getResourceType())
                .action(auditLog.getAction()).ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent()).requestUri(auditLog.getRequestUri())
                .requestParams(auditLog.getRequestParams()).result(auditLog.getResult())
                .errorMessage(auditLog.getErrorMessage()).traceId(auditLog.getTraceId())
                .spanId(auditLog.getSpanId()).parentSpanId(auditLog.getParentSpanId())
                .encryptedFields(auditLog.getEncryptedFields() != null
                        ? String.join(",", auditLog.getEncryptedFields())
                        : null)
                .createdAt(auditLog.getCreatedAt()).build();
    }

    public AuditLog toDomain(AuditLogPO po) {
        if (po == null) {
            return null;
        }

        List<String> encryptedFields =
                po.getEncryptedFields() != null ? Arrays.asList(po.getEncryptedFields().split(","))
                        : Collections.emptyList();

        return AuditLog.builder().id(po.getId()).eventId(po.getEventId())
                .sourceService(po.getSourceService()).tenantId(po.getTenantId())
                .userId(po.getUserId()).username(po.getUsername()).eventType(po.getEventType())
                .eventCategory(po.getEventCategory()).resourceId(po.getResourceId())
                .resourceType(po.getResourceType()).action(po.getAction())
                .ipAddress(po.getIpAddress()).userAgent(po.getUserAgent())
                .requestUri(po.getRequestUri()).requestParams(po.getRequestParams())
                .result(po.getResult()).errorMessage(po.getErrorMessage()).traceId(po.getTraceId())
                .spanId(po.getSpanId()).parentSpanId(po.getParentSpanId())
                .encryptedFields(encryptedFields).createdAt(po.getCreatedAt()).build();
    }
}
