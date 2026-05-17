package iam.platform.auth.application.service.routing;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import iam.platform.auth.application.service.CasTicketService;
import iam.platform.auth.domain.model.valueobject.AuthenticationResult;

/**
 * CAS (Central Authentication Service) protocol adapter.
 * Handles CAS Service Ticket generation and validation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CasProtocolAdapter implements ProtocolAdapter {

    private final CasTicketService casTicketService;

    @Override
    public boolean matches(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        return requestUri.contains("/cas/");
    }

    @Override
    public ProtocolRoute resolve(ProtocolContext context) {
        AuthenticationResult result = context.getAuthenticationResult();
        String savedRequestUrl = context.getSavedRequestUrl();

        // Extract CAS service URL from the saved request or context
        String service = extractServiceUrl(savedRequestUrl);
        
        if (service == null || service.isBlank()) {
            log.warn("CAS service URL not found, falling back to default redirect");
            return ProtocolRoute.defaultRedirect(context.getDefaultUrl());
        }

        if (result == null) {
            log.warn("Authentication result is null, cannot generate CAS ticket");
            return ProtocolRoute.defaultRedirect(context.getDefaultUrl());
        }

        // Generate CAS Service Ticket
        String ticket = casTicketService.createServiceTicket(result, service);
        
        log.debug("CAS Service Ticket generated for service: {}", service);
        return ProtocolRoute.casTicket(ticket, service);
    }

    /**
     * Extract the CAS service URL from the saved request URL.
     * The service URL is typically passed as a query parameter: ?service=https://app.example.com/callback
     */
    private String extractServiceUrl(String savedRequestUrl) {
        if (savedRequestUrl == null || savedRequestUrl.isBlank()) {
            return null;
        }

        // Check if the URL contains a service parameter
        if (savedRequestUrl.contains("service=")) {
            try {
                int startIndex = savedRequestUrl.indexOf("service=") + 8;
                int endIndex = savedRequestUrl.indexOf("&", startIndex);
                if (endIndex == -1) {
                    endIndex = savedRequestUrl.length();
                }
                return java.net.URLDecoder.decode(
                        savedRequestUrl.substring(startIndex, endIndex), "UTF-8");
            } catch (Exception e) {
                log.warn("Failed to extract service URL from: {}", savedRequestUrl);
            }
        }

        // Fallback: use the saved request URL directly
        return savedRequestUrl;
    }
}
