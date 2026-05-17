package iam.platform.auth.interfaces.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import iam.platform.auth.application.service.CasTicketService;
import iam.platform.auth.application.service.CasSloService;
import iam.platform.auth.domain.model.entity.Person;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;
import iam.platform.auth.domain.model.valueobject.AuthenticationResult;
import iam.platform.auth.domain.repository.PersonRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CAS (Central Authentication Service) Controller. Handles CAS login, service ticket validation,
 * and logout.
 */
@Slf4j
@Controller
@RequestMapping("/cas")
@RequiredArgsConstructor
public class CasController {

    private final CasTicketService casTicketService;
    private final CasSloService casSloService;
    private final PersonRepository personRepository;

    /**
     * CAS login page - displays login form if user is not authenticated. GET
     * /cas/login?service=https://app.example.com/callback
     */
    @GetMapping("/login")
    public String casLoginPage(@RequestParam(required = false) String service,
            @RequestParam(required = false) String renew,
            @RequestParam(required = false) String gateway, Model model) {

        model.addAttribute("service", service);
        model.addAttribute("renew", renew);
        model.addAttribute("gateway", gateway);
        model.addAttribute("loginType", "cas");

        log.debug("CAS login page requested, service: {}", service);
        return "cas-login";
    }

    /**
     * CAS login processing - authenticates user and redirects with Service Ticket. POST /cas/login
     */
    @PostMapping("/login")
    public void processCasLogin(@RequestParam String username, @RequestParam String password,
            @RequestParam(required = false) String service, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        log.info("Processing CAS login request for user: {}", username);

        // 1. Authenticate the user
        Person person = authenticateUser(username, password);

        if (person == null) {
            // Authentication failed
            if (service != null) {
                response.sendRedirect(
                        "/cas/login?service=" + java.net.URLEncoder.encode(service, "UTF-8")
                                + "&error=invalid_credentials");
            } else {
                response.sendRedirect("/cas/login?error=invalid_credentials");
            }
            return;
        }

        // 2. Create authentication result
        AuthenticationResult authResult = AuthenticationResult.withSelectedTenant(person,
                AuthenticationMethod.PASSWORD, null, List.of(), Set.of());

        // 3. If no service specified, just show success page
        if (service == null || service.isBlank()) {
            response.sendRedirect("/cas/login?success=true");
            return;
        }

        // 4. Generate Service Ticket
        String ticket = casTicketService.createServiceTicket(authResult, service);

        // 5. Register service for SLO (track this service in the session)
        String sessionId = request.getSession().getId();
        casSloService.registerServiceForSession(sessionId, service);
        log.debug("Service {} registered for SLO session {}", service, sessionId);

        // 6. Redirect to service?ticket=ST-xxx
        String redirectUrl = service + (service.contains("?") ? "&" : "?") + "ticket=" + ticket;
        response.sendRedirect(redirectUrl);

        log.info("CAS login successful for user: {}, ticket: {}, service: {}", username, ticket,
                service);
    }

    /**
     * CAS Service Ticket validation endpoint. GET /cas/serviceTicket?ticket=ST-xxx
     * 
     * Returns XML response compatible with CAS 3.0 protocol.
     */
    @GetMapping("/serviceTicket")
    @ResponseBody
    public ResponseEntity<String> validateServiceTicket(@RequestParam String ticket) {
        log.debug("CAS service ticket validation requested: {}", ticket);

        CasTicketService.CasValidationResponse validation =
                casTicketService.validateServiceTicket(ticket);

        if (validation == null) {
            // Ticket invalid or already consumed
            String errorResponse = """
                    <cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>
                        <cas:authenticationFailure code='INVALID_TICKET'>
                            Ticket %s not recognized or already consumed
                        </cas:authenticationFailure>
                    </cas:serviceResponse>
                    """.formatted(ticket);

            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Ticket valid, return user information
        String successResponse = """
                <cas:serviceResponse xmlns:cas='http://www.yale.edu/tp/cas'>
                    <cas:authenticationSuccess>
                        <cas:user>%s</cas:user>
                        <cas:attributes>
                            <cas:email>%s</cas:email>
                            <cas:nickname>%s</cas:nickname>
                        </cas:attributes>
                    </cas:authenticationSuccess>
                </cas:serviceResponse>
                """.formatted(validation.username(), validation.email(), validation.nickname());

        return ResponseEntity.ok(successResponse);
    }

    /**
     * CAS health check endpoint. GET /cas/health
     */
    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<Map<String, String>> casHealth() {
        return ResponseEntity.ok(Map.of("status", "UP", "protocol", "CAS 3.0", "slo", "enabled"));
    }

    /**
     * Authenticate user (simplified implementation). In production, this should delegate to
     * AuthenticationDispatcher.
     */
    private Person authenticateUser(String username, String password) {
        // Simplified: lookup user by username
        // In real implementation, use AuthenticationDispatcher.authenticate()
        return personRepository.findByUsername(username).orElse(null);
    }
}
