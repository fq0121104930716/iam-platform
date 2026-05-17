package iam.platform.auth.interfaces.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller placeholder for direct authentication API (internal services only).
 * 
 * <b>IMPORTANT:</b> OAuth2 Password Grant should NOT be handled here. Clients should use the
 * standard Spring Authorization Server endpoint:
 * 
 * <pre>
 * POST /oauth2/token
 * Content-Type: application/x-www-form-urlencoded
 * 
 * grant_type=password
 * &amp;username=admin
 * &amp;password=secret
 * &amp;client_id=legacy-app
 * &amp;client_secret=secret
 * </pre>
 * 
 * The /oauth2/token endpoint is handled by Spring Authorization Server automatically, which:
 * <ul>
 * <li>Validates client credentials</li>
 * <li>Authenticates user via PasswordAuthenticationStrategy</li>
 * <li>Runs Pre/Post Authentication Pipelines</li>
 * <li>Generates JWT tokens (Access Token, Refresh Token, ID Token)</li>
 * <li>Applies TokenCustomizer for custom claims</li>
 * </ul>
 * 
 * This controller is kept only for internal service authentication scenarios that don't need OAuth2
 * tokens.
 * 
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.3">RFC 6749 - Resource Owner Password
 *      Credentials Grant</a>
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    // TODO: Add internal authentication endpoints if needed
    // For Password Grant, always use POST /oauth2/token (Spring AS standard endpoint)
}
