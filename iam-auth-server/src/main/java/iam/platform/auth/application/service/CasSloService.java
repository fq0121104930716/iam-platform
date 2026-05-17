package iam.platform.auth.application.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import iam.platform.auth.infrastructure.config.CasProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing CAS Single Logout (SLO). Handles session tracking, ticket cleanup, and
 * logout coordination across services.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CasSloService {

    private final StringRedisTemplate stringRedisTemplate;
    private final CasProperties casProperties;

    private static final String SERVICE_KEY_PREFIX = "auth:cas:session:services:";
    private static final String LOGOUT_KEY_PREFIX = "auth:cas:logout:";

    // Fallback in-memory storage
    private final Map<String, Set<String>> sessionServicesMap = new ConcurrentHashMap<>();
    private final Set<String> invalidatedSessions = ConcurrentHashMap.newKeySet();

    /**
     * Register a service for a session (called when ST is created).
     *
     * @param sessionId The session ID (TGT)
     * @param service The service URL
     */
    public void registerServiceForSession(String sessionId, String service) {
        String key = SERVICE_KEY_PREFIX + sessionId;

        try {
            stringRedisTemplate.opsForSet().add(key, service);
            stringRedisTemplate.expire(key,
                    Duration.ofHours(casProperties.getTicketValiditySeconds() / 3600 + 1));
            log.debug("Service registered for session {}: {}", sessionId, service);
        } catch (Exception e) {
            log.warn("Redis unavailable, using fallback storage for session registration");
            sessionServicesMap.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet())
                    .add(service);
        }
    }

    /**
     * Get all services registered for a session.
     *
     * @param sessionId The session ID
     * @return List of service URLs
     */
    public List<String> getServicesForSession(String sessionId) {
        String key = SERVICE_KEY_PREFIX + sessionId;

        try {
            Set<String> services = stringRedisTemplate.opsForSet().members(key);
            return services != null ? new ArrayList<>(services) : List.of();
        } catch (Exception e) {
            log.warn("Redis unavailable, checking fallback storage");
            return new ArrayList<>(sessionServicesMap.getOrDefault(sessionId, Set.of()));
        }
    }

    /**
     * Get current session ID from request.
     *
     * @param request HTTP request
     * @return Session ID or null
     */
    public String getCurrentSessionId(HttpServletRequest request) {
        try {
            if (request.getSession(false) != null) {
                return request.getSession().getId();
            }
        } catch (Exception e) {
            log.warn("Failed to get session ID: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Process Back Channel Logout request.
     *
     * @param logoutRequest Base64 encoded logout request
     */
    public void processBackChannelLogout(String logoutRequest) {
        try {
            // Decode the logout request
            String decodedRequest =
                    new String(Base64.getDecoder().decode(logoutRequest), StandardCharsets.UTF_8);

            log.debug("Processing Back Channel Logout: {}", decodedRequest);

            // Extract session index or name ID from the request
            String sessionId = extractSessionIdFromLogoutRequest(decodedRequest);

            if (sessionId != null) {
                // Invalidate the session and all associated tickets
                invalidateSession(sessionId);
                log.info("Back Channel Logout completed for session: {}", sessionId);
            } else {
                log.warn("Could not extract session ID from logout request");
            }
        } catch (Exception e) {
            log.error("Failed to process Back Channel Logout: {}", e.getMessage(), e);
            throw new RuntimeException("Invalid logout request", e);
        }
    }

    /**
     * Extract session ID from SAML logout request. This is a simplified implementation - in
     * production, use proper SAML parsing.
     */
    private String extractSessionIdFromLogoutRequest(String logoutRequest) {
        // Look for SessionIndex element
        int startIndex = logoutRequest.indexOf("<samlp:SessionIndex>");
        if (startIndex == -1) {
            startIndex = logoutRequest.indexOf("<SessionIndex>");
        }

        if (startIndex != -1) {
            int contentStart = logoutRequest.indexOf(">", startIndex) + 1;
            int contentEnd = logoutRequest.indexOf("</", contentStart);
            if (contentEnd != -1) {
                return logoutRequest.substring(contentStart, contentEnd).trim();
            }
        }

        return null;
    }

    /**
     * Mark a service as logged out in Front Channel Logout.
     *
     * @param sessionId The session ID
     * @param service The service URL
     */
    public void markServiceLoggedOut(String sessionId, String service) {
        String logoutKey = LOGOUT_KEY_PREFIX + sessionId + ":" + service;

        try {
            stringRedisTemplate.opsForValue().set(logoutKey, "logged_out", Duration.ofMinutes(5));
            log.debug("Service marked as logged out: {}", service);
        } catch (Exception e) {
            log.warn("Failed to mark service as logged out in Redis");
        }
    }

    /**
     * Continue Front Channel Logout to next service.
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param completedService The service that just completed logout
     */
    public void continueFrontChannelLogout(HttpServletRequest request, HttpServletResponse response,
            String completedService) throws IOException {
        String sessionId = getCurrentSessionId(request);

        if (sessionId == null) {
            response.sendRedirect("/cas/login?logout=true");
            return;
        }

        // Get all services for this session
        List<String> services = getServicesForSession(sessionId);

        // Find next service that hasn't logged out yet
        for (String service : services) {
            if (!service.equals(completedService)) {
                String logoutKey = LOGOUT_KEY_PREFIX + sessionId + ":" + service;

                try {
                    String status = stringRedisTemplate.opsForValue().get(logoutKey);
                    if (status == null) {
                        // This service hasn't logged out yet, redirect to it
                        String logoutUrl = service + (service.contains("?") ? "&" : "?")
                                + "logoutRequest=" + buildLogoutRequest(sessionId);
                        response.sendRedirect(logoutUrl);
                        return;
                    }
                } catch (Exception e) {
                    log.warn("Failed to check logout status for service: {}", service);
                }
            }
        }

        // All services logged out, complete the logout
        response.sendRedirect("/cas/logout?sessionId=" + sessionId);
    }

    /**
     * Build a logout request for Front Channel Logout.
     *
     * @param sessionId The session ID
     * @return Base64 encoded logout request
     */
    private String buildLogoutRequest(String sessionId) {
        String logoutRequest = """
                <samlp:LogoutRequest xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol"
                                     xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
                                     ID="_%s"
                                     Version="2.0"
                                     IssueInstant="%s">
                    <saml:Issuer>https://sso.example.com</saml:Issuer>
                    <samlp:SessionIndex>%s</samlp:SessionIndex>
                </samlp:LogoutRequest>
                """.formatted(UUID.randomUUID().toString(), new Date().toString(), sessionId);

        return Base64.getEncoder().encodeToString(logoutRequest.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Process logout response from a service.
     *
     * @param logoutResponse Base64 encoded logout response
     */
    public void processLogoutResponse(String logoutResponse) {
        try {
            String decodedResponse =
                    new String(Base64.getDecoder().decode(logoutResponse), StandardCharsets.UTF_8);

            log.debug("Processing logout response: {}", decodedResponse);

            // Extract status from response
            if (decodedResponse.contains("Success")) {
                log.info("Logout response successful");
            } else {
                log.warn("Logout response indicates failure");
            }
        } catch (Exception e) {
            log.error("Failed to process logout response: {}", e.getMessage(), e);
        }
    }

    /**
     * Invalidate a session and clean up all associated tickets.
     *
     * @param sessionId The session ID to invalidate
     */
    public void invalidateSession(String sessionId) {
        try {
            // Get all services for this session
            List<String> services = getServicesForSession(sessionId);

            // Clean up all service tickets for these services
            for (String service : services) {
                cleanupTicketsForService(sessionId, service);
            }

            // Remove session tracking
            String serviceKey = SERVICE_KEY_PREFIX + sessionId;
            try {
                stringRedisTemplate.delete(serviceKey);
            } catch (Exception e) {
                log.warn("Failed to delete session services from Redis");
            }
            sessionServicesMap.remove(sessionId);

            // Mark session as invalidated
            invalidatedSessions.add(sessionId);

            log.info("Session {} invalidated, {} services cleaned up", sessionId, services.size());
        } catch (Exception e) {
            log.error("Failed to invalidate session {}: {}", sessionId, e.getMessage(), e);
        }
    }

    /**
     * Clean up tickets for a specific service.
     *
     * @param sessionId The session ID
     * @param service The service URL
     */
    private void cleanupTicketsForService(String sessionId, String service) {
        // In a full implementation, you would track all STs per service and delete them
        // For now, this is handled by the TTL expiration in CasTicketService
        log.debug("Cleaning up tickets for service: {}", service);
    }

    /**
     * Check if a session has been invalidated.
     *
     * @param sessionId The session ID
     * @return true if invalidated
     */
    public boolean isSessionInvalidated(String sessionId) {
        return invalidatedSessions.contains(sessionId);
    }
}
