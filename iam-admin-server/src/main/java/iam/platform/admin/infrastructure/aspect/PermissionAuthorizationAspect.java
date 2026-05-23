package iam.platform.admin.infrastructure.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import iam.platform.common.model.annotation.RequirePermission;
import iam.platform.common.model.exception.AccessDeniedException;
import iam.platform.admin.domain.service.PermissionEvaluationService;
import iam.platform.common.context.TenantContext;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * AOP aspect that intercepts methods annotated with @RequirePermission and enforces permission
 * checks before method execution.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@org.springframework.core.annotation.Order(Ordered.HIGHEST_PRECEDENCE)
public class PermissionAuthorizationAspect {

    private final PermissionEvaluationService permissionService;

    @Around("@annotation(iam.platform.common.model.annotation.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);
        if (requirePermission == null) {
            return joinPoint.proceed();
        }

        Long tenantAccountId = TenantContext.getCurrentTenantAccountId();
        if (tenantAccountId == null) {
            log.warn("Permission check failed: no tenant context for method {}",
                    joinPoint.getSignature());
            throw new AccessDeniedException("No tenant context");
        }

        boolean granted = evaluate(requirePermission, tenantAccountId);
        if (!granted) {
            String requiredPerm = getRequiredPermissionDescription(requirePermission);
            log.warn("Access denied: tenant account {} lacks required permissions: {}",
                    tenantAccountId, requiredPerm);
            throw new AccessDeniedException(requiredPerm);
        }

        return joinPoint.proceed();
    }

    private boolean evaluate(RequirePermission annotation, Long tenantAccountId) {
        // Priority: value (single) → allOf (AND) → anyOf (OR)
        if (!annotation.value().isEmpty()) {
            return permissionService.hasPermission(tenantAccountId, annotation.value());
        }
        if (annotation.allOf().length > 0) {
            return permissionService.hasAllPermissions(tenantAccountId, Set.of(annotation.allOf()));
        }
        if (annotation.anyOf().length > 0) {
            return permissionService.hasAnyPermission(tenantAccountId, Set.of(annotation.anyOf()));
        }
        // No permission required
        return true;
    }

    private String getRequiredPermissionDescription(RequirePermission annotation) {
        if (!annotation.value().isEmpty()) {
            return annotation.value();
        }
        if (annotation.allOf().length > 0) {
            return "allOf: [" + String.join(", ", annotation.allOf()) + "]";
        }
        if (annotation.anyOf().length > 0) {
            return "anyOf: [" + String.join(", ", annotation.anyOf()) + "]";
        }
        return "none";
    }
}
