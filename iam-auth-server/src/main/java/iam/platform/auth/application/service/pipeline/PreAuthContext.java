package iam.platform.auth.application.service.pipeline;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import iam.platform.auth.domain.model.valueobject.AuthenticationCredentials;

import java.util.HashMap;
import java.util.Map;

/**
 * Mutable context object that carries information through the pre-authentication pipeline.
 */
@Getter
public class PreAuthContext {
    private final AuthenticationCredentials credentials;
    private final HttpServletRequest request;
    private final String clientIp;
    private final Map<String, Object> attributes = new HashMap<>();
    private String identifierKey;

    private PreAuthContext(AuthenticationCredentials credentials, HttpServletRequest request, String clientIp) {
        this.credentials = credentials;
        this.request = request;
        this.clientIp = clientIp;
    }

    /**
     * Create a PreAuthContext from the authentication token and HTTP request.
     */
    public static PreAuthContext from(AuthenticationCredentials credentials, HttpServletRequest request) {
        String clientIp = extractClientIp(request);
        return new PreAuthContext(credentials, request, clientIp);
    }

    /**
     * Set an attribute in the context for sharing data between handlers.
     */
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    /**
     * Get an attribute from the context.
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    /**
     * Set the identifier key used for rate limiting and lockout tracking.
     */
    public void setIdentifierKey(String identifierKey) {
        this.identifierKey = identifierKey;
    }

    /**
     * Extract the client IP address from the request, considering proxy headers.
     */
    private static String extractClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return ip.split(",")[0].trim();
        }

        ip = request.getHeader("X-Real-IP");
        if (ip != null && !ip.isBlank()) {
            return ip;
        }

        return request.getRemoteAddr();
    }
}
