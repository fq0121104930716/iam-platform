package iam.platform.auth.interfaces.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import iam.platform.auth.application.service.CasSloService;

import java.io.IOException;
import java.util.List;

/**
 * CAS Single Logout (SLO) Handler. Handles both Back Channel Logout (server-to-server) and Front
 * Channel Logout (browser redirect).
 */
@Slf4j
@Controller
@RequestMapping("/cas")
@RequiredArgsConstructor
public class CasSloHandler {

    private final CasSloService casSloService;

    /**
     * Initiate CAS logout (Front Channel). GET /cas/logout
     * 
     * Destroys the TGT and redirects all registered services to logout.
     * 
     * @param request HTTP request
     * @param service Optional service URL to redirect to after logout
     * @return logout page or redirect to service
     */
    @GetMapping("/logout")
    public String initiateLogout(HttpServletRequest request,
            @RequestParam(required = false) String service, Model model) {
        log.info("CAS logout initiated");

        // Get the current session ID (TGT)
        String sessionId = casSloService.getCurrentSessionId(request);

        if (sessionId != null) {
            // Get all services that were authenticated in this session
            List<String> services = casSloService.getServicesForSession(sessionId);

            if (!services.isEmpty()) {
                // Front Channel Logout: redirect services one by one
                model.addAttribute("services", services);
                model.addAttribute("service", service);
                model.addAttribute("sessionId", sessionId);
                log.info("Front Channel Logout to {} services", services.size());
                return "cas-logout-redirect";
            }
        }

        // No services to logout, complete logout directly
        return completeLogout(request, service);
    }

    /**
     * Back Channel Logout endpoint. POST /cas/logout/backChannel
     * 
     * Receives logout requests from other CAS servers or services.
     * 
     * @param logoutRequest SAML logout request (base64 encoded)
     * @param response HTTP response
     */
    @PostMapping("/logout/backChannel")
    @ResponseBody
    public ResponseEntity<String> handleBackChannelLogout(@RequestParam String logoutRequest,
            HttpServletResponse response) {

        log.info("Back Channel Logout request received");

        try {
            // Decode and process the logout request
            casSloService.processBackChannelLogout(logoutRequest);

            // Return success response
            String successResponse = """
                    <cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>
                        <cas:logoutSuccess/>
                    </cas:serviceResponse>
                    """;

            return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(successResponse);

        } catch (Exception e) {
            log.error("Failed to process Back Channel Logout: {}", e.getMessage(), e);

            String errorResponse = """
                    <cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>
                        <cas:logoutFailure code='INVALID_REQUEST'>
                            Failed to process logout request: %s
                        </cas:logoutFailure>
                    </cas:serviceResponse>
                    """.formatted(e.getMessage());

            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_XML)
                    .body(errorResponse);
        }
    }

    /**
     * Front Channel Logout callback. GET /cas/logout/frontChannel
     * 
     * Called by services after they logout on the front channel.
     * 
     * @param service The service that completed logout
     * @param response HTTP response
     */
    @GetMapping("/logout/frontChannel")
    public void handleFrontChannelCallback(@RequestParam String service, HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        log.info("Front Channel Logout callback from service: {}", service);

        // Mark this service as logged out
        String sessionId = casSloService.getCurrentSessionId(request);
        if (sessionId != null) {
            casSloService.markServiceLoggedOut(sessionId, service);
        }

        // Continue with next service or complete logout
        casSloService.continueFrontChannelLogout(request, response, service);
    }

    /**
     * Logout response endpoint. GET /cas/logoutResponse
     * 
     * Receives logout responses from services.
     * 
     * @param logoutResponse SAML logout response
     * @param RelayState Relay state parameter
     */
    @GetMapping("/logoutResponse")
    public String handleLogoutResponse(@RequestParam(required = false) String logoutResponse,
            @RequestParam(required = false) String RelayState,
            @RequestParam(required = false) String service, HttpServletRequest request) {

        log.info("Logout response received");

        try {
            if (logoutResponse != null) {
                casSloService.processLogoutResponse(logoutResponse);
            }

            // Complete the logout process
            return completeLogout(request, service);

        } catch (Exception e) {
            log.error("Failed to process logout response: {}", e.getMessage(), e);
            return "redirect:/cas/login?logout_error=true";
        }
    }

    /**
     * Complete the logout process by invalidating session and cleaning up tickets.
     */
    private String completeLogout(HttpServletRequest request, String service) {
        try {
            String sessionId = casSloService.getCurrentSessionId(request);

            if (sessionId != null) {
                // Clean up all tickets for this session
                casSloService.invalidateSession(sessionId);
                log.info("Session {} invalidated, all tickets cleaned up", sessionId);
            }

            // Invalidate HTTP session
            if (request.getSession(false) != null) {
                request.getSession().invalidate();
            }

            log.info("CAS logout completed");

            // Redirect to service if provided
            if (service != null && !service.isBlank()) {
                return "redirect:" + service;
            }

            return "redirect:/cas/login?logout=true";

        } catch (Exception e) {
            log.error("Error during logout completion: {}", e.getMessage(), e);
            return "redirect:/cas/login?logout_error=true";
        }
    }
}
