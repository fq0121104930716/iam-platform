package iam.platform.admin.infrastructure.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import iam.platform.common.model.annotation.AuditLog;
import iam.platform.common.model.enums.AuditResult;
import iam.platform.common.context.TenantContext;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Builder for constructing AuditLogContext from the current request and method execution context.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogContextBuilder {

    private final ObjectMapper objectMapper;

    /**
     * Build audit context from the annotation and method arguments.
     */
    public AuditLogContext build(ProceedingJoinPoint joinPoint, AuditLog annotation,
            Object[] args) {
        String[] paramNames = getParameterNames(joinPoint);
        Map<String, Object> paramMap = buildParamMap(paramNames, args);

        // Mask sensitive fields
        if (annotation.logParams() && annotation.sensitiveFields().length > 0) {
            maskSensitiveFields(paramMap, annotation.sensitiveFields());
        }

        String requestParams = null;
        if (annotation.logParams()) {
            requestParams = toJson(paramMap);
            // Truncate if too large
            if (requestParams != null && requestParams.length() > 10000) {
                requestParams = requestParams.substring(0, 10000) + "...[truncated]";
            }
        }

        return AuditLogContext.builder().eventId(UUID.randomUUID().toString())
                .sourceService("iam-admin-service").tenantId(TenantContext.getCurrentTenantId())
                .userId(TenantContext.getCurrentUserId()).username(getCurrentUsername())
                .eventType(annotation.value()).resourceType(annotation.resourceType())
                .action(resolveAction(annotation, paramMap)).ipAddress(getClientIp())
                .userAgent(getUserAgent()).requestUri(getRequestUri()).requestParams(requestParams)
                .result(AuditResult.SUCCESS).traceId(extractTraceId()).build();
    }

    private String[] getParameterNames(ProceedingJoinPoint joinPoint) {
        // Use generic names if actual names are not available
        Object[] args = joinPoint.getArgs();
        String[] names = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            names[i] = "arg" + i;
        }
        return names;
    }

    private Map<String, Object> buildParamMap(String[] names, Object[] args) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < names.length; i++) {
            if (args[i] != null && !isIgnoredType(args[i])) {
                map.put(names[i], args[i]);
            }
        }
        return map;
    }

    private boolean isIgnoredType(Object arg) {
        // Ignore HttpServletRequest, HttpServletResponse, etc.
        String className = arg.getClass().getName();
        return className.contains("HttpServletRequest") || className.contains("HttpServletResponse")
                || className.contains("Servlet");
    }

    private void maskSensitiveFields(Map<String, Object> paramMap, String[] sensitiveFields) {
        for (String field : sensitiveFields) {
            for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(field) || entry.getKey().contains(field)) {
                    entry.setValue("***MASKED***");
                }
                // Also handle nested objects (basic handling)
                if (entry.getValue() instanceof Map) {
                    maskNestedMap((Map<?, ?>) entry.getValue(), field);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void maskNestedMap(Map<?, ?> map, String field) {
        Map<String, Object> typedMap = (Map<String, Object>) map;
        for (Map.Entry<String, Object> entry : typedMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(field) || entry.getKey().contains(field)) {
                entry.setValue("***MASKED***");
            }
            if (entry.getValue() instanceof Map) {
                maskNestedMap((Map<?, ?>) entry.getValue(), field);
            }
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj != null ? obj.toString() : null;
        }
    }

    private String resolveAction(AuditLog annotation, Map<String, Object> paramMap) {
        String template = annotation.action();
        if (template.isEmpty()) {
            return annotation.value().name();
        }
        // Simple SpEL-like replacement: #{#paramName} -> value
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            String placeholder = "#{" + "#" + entry.getKey() + "}";
            template = template.replace(placeholder, String.valueOf(entry.getValue()));
        }
        return template;
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "anonymous";
    }

    private String getClientIp() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null)
            return "unknown";

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getUserAgent() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null)
            return "unknown";
        String ua = request.getHeader("User-Agent");
        if (ua == null) {
            return "unknown";
        }
        return ua.length() <= 500 ? ua : ua.substring(0, 500);
    }

    private String getRequestUri() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null)
            return "unknown";
        return request.getRequestURI();
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attrs != null ? attrs.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String extractTraceId() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        // Try common tracing headers in order of preference
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            traceId = request.getHeader("X-B3-TraceId");
        }
        if (traceId == null || traceId.isEmpty()) {
            traceId = request.getHeader("traceparent");
            // W3C Trace Context format: version-traceId-spanId-flags
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
