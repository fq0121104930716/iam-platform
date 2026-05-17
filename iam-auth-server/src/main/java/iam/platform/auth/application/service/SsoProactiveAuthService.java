package iam.platform.auth.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Service for SSO proactive authorization code push.
 *
 * This service validates client configuration and constructs the authorization URL for internal
 * forwarding to Spring Authorization Server.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SsoProactiveAuthService {

    private final RegisteredClientRepository clientRepository;

    /**
     * Build the authorization URL for proactive code push.
     *
     * @param clientId the client ID
     * @param state optional state parameter
     * @param nonce optional nonce for OIDC
     * @param codeChallenge optional PKCE code challenge
     * @param codeChallengeMethod PKCE code challenge method
     * @return the authorization URL
     * @throws IllegalArgumentException if client is not found or misconfigured
     */
    public String buildAuthorizationUrl(
            String clientId,
            String state,
            String nonce,
            String codeChallenge,
            String codeChallengeMethod) {

        // 1. Find client configuration
        RegisteredClient client = clientRepository.findByClientId(clientId);
        if (client == null) {
            throw new IllegalArgumentException("Invalid client_id: " + clientId);
        }

        // 2. Get redirect URI (use the first registered one)
        Set<String> redirectUris = client.getRedirectUris();
        if (redirectUris == null || redirectUris.isEmpty()) {
            throw new IllegalArgumentException("No redirect URIs registered for client: " + clientId);
        }
        String redirectUri = redirectUris.iterator().next();

        // 3. Get scopes
        Set<String> scopes = client.getScopes();
        String scopeParam = scopes != null && !scopes.isEmpty()
                ? String.join(" ", scopes)
                : "openid profile";

        // 4. Construct authorization URL
        StringBuilder url = new StringBuilder("/oauth2/authorize?");
        url.append("response_type=code");
        url.append("&client_id=").append(URLEncoder.encode(clientId, StandardCharsets.UTF_8));
        url.append("&redirect_uri=").append(URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
        url.append("&scope=").append(URLEncoder.encode(scopeParam, StandardCharsets.UTF_8));

        if (state != null && !state.isBlank()) {
            url.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));
        }

        if (nonce != null && !nonce.isBlank()) {
            url.append("&nonce=").append(URLEncoder.encode(nonce, StandardCharsets.UTF_8));
        }

        // PKCE support
        if (codeChallenge != null && !codeChallenge.isBlank()) {
            url.append("&code_challenge=").append(URLEncoder.encode(codeChallenge, StandardCharsets.UTF_8));
            url.append("&code_challenge_method=").append(codeChallengeMethod);
        }

        log.debug("Built authorization URL for proactive push: client={}, redirect_uri={}",
                clientId, redirectUri);

        return url.toString();
    }
}
