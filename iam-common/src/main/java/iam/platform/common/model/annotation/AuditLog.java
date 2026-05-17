package iam.platform.common.model.annotation;

import iam.platform.common.model.enums.AuditEventType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for audit logging. When applied, the method execution will be
 * automatically recorded in the audit log.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {

    /**
     * The type of audit event.
     */
    AuditEventType value();

    /**
     * The type of resource being operated on (e.g., "user", "tenant", "role").
     */
    String resourceType() default "";

    /**
     * Description template for the action. Supports SpEL expressions. Example: "Created tenant
     * #{#request.tenantName}"
     */
    String action() default "";

    /**
     * Whether to log request parameters.
     */
    boolean logParams() default true;

    /**
     * Field names that should be masked in the log (e.g., password, secret, token).
     */
    String[] sensitiveFields() default {"password", "secret", "token"};
}
