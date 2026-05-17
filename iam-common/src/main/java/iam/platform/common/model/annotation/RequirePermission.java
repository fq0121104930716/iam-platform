package iam.platform.common.model.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enforce permission checks on methods. When applied, the method execution will be
 * intercepted by PermissionAuthorizationAspect to verify the current user has the required
 * permissions.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * Single permission code required to access the method. Example: "user:write"
     */
    String value() default "";

    /**
     * At least one of these permissions is required (OR logic). Example: {"user:read",
     * "user:export"}
     */
    String[] anyOf() default {};

    /**
     * All of these permissions are required (AND logic). Example: {"user:read", "user:write"}
     */
    String[] allOf() default {};
}
