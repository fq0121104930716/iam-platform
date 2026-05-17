package iam.platform.auth.interfaces.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import iam.platform.auth.application.service.SsoProactiveAuthService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * REST controller for SSO proactive authorization code push.
 *
 * Allows the SSO platform to proactively generate an Authorization Code and redirect to an
 * application without waiting for the client to initiate the authorization request.
 */
@Slf4j
@RestController
@RequestMapping("/api/sso")
@RequiredArgsConstructor
public class SsoProactiveAuthController {

    private final SsoProactiveAuthService proactiveAuthService;

    /**
     * SSO proactive authorization code endpoint.
     *
     * Principle: Simulate an authorization request on the server side, utilize Spring Authorization
     * Server's standard flow to generate a Code, then manually extract the Code and redirect to
     * the application's redirect_uri.
     *
     * @param clientId the client ID of the target application
     * @param state optional state parameter for CSRF protection
     * @param nonce optional nonce for OIDC
     * @param codeChallenge optional PKCE code challenge
     * @param codeChallengeMethod optional PKCE code challenge method (default: S256)
     * @param request HTTP servlet request
     * @param response HTTP servlet response
     */
    @GetMapping("/push/{clientId}")
    @PreAuthorize("isAuthenticated()")
    public void pushAuthCode(
            @PathVariable String clientId,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String nonce,
            @RequestParam(required = false) String codeChallenge,
            @RequestParam(value = "code_challenge_method", defaultValue = "S256") String codeChallengeMethod,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        log.info("SSO proactive push auth code initiated for client: {}", clientId);

        try {
            // Delegate to service for validation and URL construction
            String authUrl = proactiveAuthService.buildAuthorizationUrl(
                    clientId, state, nonce, codeChallenge, codeChallengeMethod);

            // Forward to the authorization endpoint internally
            // Spring AS will detect the user is already authenticated and generate a Code
            log.debug("Forwarding to authorization URL: {}", authUrl);
            request.getRequestDispatcher(authUrl).forward(request, response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid client configuration: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to process proactive auth code push", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    /**
     * Fallback endpoint for when user is not authenticated.
     * Redirects to login page with return URL.
     */
    @GetMapping("/push/{clientId}/login")
    public void redirectToLogin(
            @PathVariable String clientId,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String nonce,
            @RequestParam(required = false) String codeChallenge,
            @RequestParam(value = "code_challenge_method", defaultValue = "S256") String codeChallengeMethod,
            HttpServletResponse response) throws IOException {

        // Build return URL to continue push flow after login
        StringBuilder returnUrl = new StringBuilder("/api/sso/push/");
        returnUrl.append(clientId);

        boolean hasParams = false;
        if (state != null) {
            returnUrl.append("?state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));
            hasParams = true;
        }
        if (nonce != null) {
            returnUrl.append(hasParams ? "&" : "?");
            returnUrl.append("nonce=").append(URLEncoder.encode(nonce, StandardCharsets.UTF_8));
            hasParams = true;
        }
        if (codeChallenge != null) {
            returnUrl.append(hasParams ? "&" : "?");
            returnUrl.append("code_challenge=").append(URLEncoder.encode(codeChallenge, StandardCharsets.UTF_8));
            returnUrl.append("&code_challenge_method=").append(codeChallengeMethod);
        }

        response.sendRedirect("/login?redirect=" + URLEncoder.encode(returnUrl.toString(), StandardCharsets.UTF_8));
    }
}
