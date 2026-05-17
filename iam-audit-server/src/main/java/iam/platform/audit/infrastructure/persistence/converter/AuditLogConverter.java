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

        return AuditLogPO.builder()
                .id(auditLog.getId())
                .eventId(auditLog.getEventId())
                .sourceService(auditLog.getSourceService())
                .tenantId(auditLog.getTenantId())
                .personId(auditLog.getPersonId())
                .username(auditLog.getUsername())
                .eventType(auditLog.getEventType() != null ? auditLog.getEventType().name() : null)
                .eventCategory(auditLog.getEventCategory() != null ? auditLog.getEventCategory().name() : null)
                .resourceId(auditLog.getResourceId())
                .resourceType(auditLog.getResourceType())
                .action(auditLog.getAction())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .requestUri(auditLog.getRequestUri())
                .requestParams(auditLog.getRequestParams())
                .result(auditLog.getResult() != null ? auditLog.getResult().name() : null)
                .errorMessage(auditLog.getErrorMessage())
                .traceId(auditLog.getTraceId())
                .encryptedFields(auditLog.getEncryptedFields() != null 
                        ? String.join(",", auditLog.getEncryptedFields()) 
                        : null)
                .createdAt(auditLog.getCreatedAt())
                .build();
    }

    public AuditLog toDomain(AuditLogPO po) {
        if (po == null) {
            return null;
        }

        List<String> encryptedFields = po.getEncryptedFields() != null 
                ? Arrays.asList(po.getEncryptedFields().split(","))
                : Collections.emptyList();

        return AuditLog.builder()
                .id(po.getId())
                .eventId(po.getEventId())
                .sourceService(po.getSourceService())
                .tenantId(po.getTenantId())
                .personId(po.getPersonId())
                .username(po.getUsername())
                .eventType(toEventType(po.getEventType()))
                .eventCategory(toEventCategory(po.getEventCategory()))
                .resourceId(po.getResourceId())
                .resourceType(po.getResourceType())
                .action(po.getAction())
                .ipAddress(po.getIpAddress())
                .userAgent(po.getUserAgent())
                .requestUri(po.getRequestUri())
                .requestParams(po.getRequestParams())
                .result(toResult(po.getResult()))
                .errorMessage(po.getErrorMessage())
                .traceId(po.getTraceId())
                .encryptedFields(encryptedFields)
                .createdAt(po.getCreatedAt())
                .build();
    }

    private iam.platform.common.model.enums.AuditEventType toEventType(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        try {
            return iam.platform.common.model.enums.AuditEventType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private iam.platform.common.model.enums.EventCategory toEventCategory(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        try {
            return iam.platform.common.model.enums.EventCategory.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private iam.platform.common.model.enums.AuditResult toResult(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        try {
            return iam.platform.common.model.enums.AuditResult.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
