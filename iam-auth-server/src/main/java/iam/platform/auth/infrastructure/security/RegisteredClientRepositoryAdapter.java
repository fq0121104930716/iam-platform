package iam.platform.auth.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;
import iam.platform.auth.domain.model.entity.Application;
import iam.platform.auth.domain.repository.ApplicationRepository;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisteredClientRepositoryAdapter implements RegisteredClientRepository {

    private final ApplicationRepository applicationRepository;

    @Override
    public void save(RegisteredClient registeredClient) {
        throw new UnsupportedOperationException("Use ApplicationManagement API instead");
    }

    @Override
    public RegisteredClient findById(String id) {
        // Try to find by Application ID first (Long type)
        try {
            Long appId = Long.parseLong(id);
            return applicationRepository.findById(appId).map(this::toRegisteredClient).orElse(null);
        } catch (NumberFormatException e) {
            // If not a valid Long, try to find by appId (String type)
            return applicationRepository.findByAppId(id).map(this::toRegisteredClient).orElse(null);
        }
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return applicationRepository.findByAppId(clientId).map(this::toRegisteredClient)
                .orElse(null);
    }

    private RegisteredClient toRegisteredClient(Application app) {
        RegisteredClient.Builder builder = RegisteredClient.withId(app.getId().toString())
                .clientId(app.getAppId()).clientName(app.getAppName());

        // Set Client Secret
        // Database storage: AES-256-GCM encrypted plaintext
        // After EncryptedStringConverter decryption: Original plaintext (e.g.
        // "demo-client-secret-2024")
        // Spring Authorization Server requires: Must be encoded with PasswordEncoder
        if (app.getAppSecret() != null && !app.getAppSecret().isBlank()) {
            // Security policy selection:
            // Option 1: {noop} plaintext - Suitable for internal environment, best performance
            builder.clientSecret("{noop}" + app.getAppSecret());
            // Option 2: BCrypt encoding - Suitable for production environment, anti-memory dump,
            // follows defense-in-depth
            // If integrated with professional KMS (e.g. HashiCorp Vault, AWS KMS), use BCrypt for
            // defense-in-depth
            // builder.clientSecret(passwordEncoder.encode(app.getAppSecret()));
        }

        // Add client authentication methods
        // Default to client_secret_basic for backward compatibility
        builder.clientAuthenticationMethod(new ClientAuthenticationMethod("client_secret_basic"));

        // Add authorization grant types
        // authorization_code: Standard OIDC/OAuth2 flow (recommended)
        // password: For legacy third-party applications (configure per-client)
        builder.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE);

        // Enable password grant for legacy clients that cannot use authorization_code flow
        // Security note: Password grant requires high-trust clients
        builder.authorizationGrantType(AuthorizationGrantType.PASSWORD);

        // Add redirect URIs
        for (String redirectUri : app.getCallbackUrls()) {
            builder.redirectUri(redirectUri);
        }

        // Add scopes
        for (String scope : app.getAllowedScopes()) {
            builder.scope(scope);
        }

        builder.tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofSeconds(app.getAccessTokenTtlSeconds()))
                .refreshTokenTimeToLive(Duration.ofSeconds(app.getRefreshTokenTtlSeconds()))
                .build());

        builder.clientSettings(ClientSettings.builder().requireProofKey(app.isRequireProofKey())
                .requireAuthorizationConsent(app.isRequireAuthorizationConsent()).build());

        return builder.build();
    }
}
