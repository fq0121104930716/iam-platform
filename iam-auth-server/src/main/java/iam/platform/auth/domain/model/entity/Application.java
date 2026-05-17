package iam.platform.auth.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import iam.platform.common.util.Guard;
import iam.platform.common.model.enums.AppStatus;
import iam.platform.common.model.enums.AppType;
import iam.platform.common.model.valueobject.AppCredential;
import iam.platform.common.model.valueobject.TokenSettings;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    private Long id;
    private String appId;
    private String appSecret;
    private String appName;
    private Long tenantId;
    private AppType appType;
    private String description;
    private String logoUrl;
    private AppStatus status;
    private String homePageUrl;
    private Set<String> callbackUrls;
    private Set<String> postLogoutRedirectUris;
    private Set<String> allowedScopes;
    private boolean requireProofKey;
    private boolean requireAuthorizationConsent;
    private int accessTokenTtlSeconds;
    private int refreshTokenTtlSeconds;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Factory Methods ====================

    /**
     * Register a new Application with auto-generated credentials and ACTIVE status.
     */
    public static Application register(String appName, Long tenantId, AppType appType,
            String description, String logoUrl, String homePageUrl,
            Set<String> callbackUrls, Set<String> postLogoutRedirectUris,
            Set<String> allowedScopes, boolean requirePkce,
            boolean requireConsent, TokenSettings tokenSettings) {
        Guard.notBlank(appName, "Application name cannot be blank");
        Guard.notNull(tokenSettings, "Token settings cannot be null");

        AppCredential credential = AppCredential.generate();

        return Application.builder()
                .appId(credential.getAppId())
                .appSecret(credential.getAppSecret())
                .appName(appName)
                .tenantId(tenantId)
                .appType(appType)
                .description(description)
                .logoUrl(logoUrl)
                .status(AppStatus.ACTIVE)
                .homePageUrl(homePageUrl)
                .callbackUrls(callbackUrls != null ? new HashSet<>(callbackUrls) : new HashSet<>())
                .postLogoutRedirectUris(
                        postLogoutRedirectUris != null ? new HashSet<>(postLogoutRedirectUris) : new HashSet<>())
                .allowedScopes(allowedScopes != null ? new HashSet<>(allowedScopes) : new HashSet<>())
                .requireProofKey(requirePkce)
                .requireAuthorizationConsent(requireConsent)
                .accessTokenTtlSeconds(tokenSettings.getAccessTokenTtlSeconds())
                .refreshTokenTtlSeconds(tokenSettings.getRefreshTokenTtlSeconds())
                .enabled(true)
                .build();
    }

    // ==================== Credential Management ====================

    /**
     * Rotate the application secret. Returns the new credential (including plain
     * secret).
     */
    public AppCredential rotateSecret() {
        AppCredential current = AppCredential.of(this.appId, this.appSecret);
        AppCredential rotated = current.rotateSecret();
        this.appSecret = rotated.getAppSecret();
        this.updatedAt = LocalDateTime.now();
        return rotated;
    }

    // ==================== Status Lifecycle ====================

    /**
     * Activate the application. Cannot activate a BLOCKED application.
     */
    public void activate() {
        Guard.state(status != AppStatus.BLOCKED,
                "Cannot activate a blocked application.");
        this.status = AppStatus.ACTIVE;
        this.enabled = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Deactivate the application. Only ACTIVE applications can be deactivated.
     */
    public void deactivate() {
        Guard.state(status == AppStatus.ACTIVE,
                "Only active applications can be deactivated.");
        this.status = AppStatus.INACTIVE;
        this.enabled = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Block the application (admin action, irreversible without unblock).
     */
    public void block() {
        this.status = AppStatus.BLOCKED;
        this.enabled = false;
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== Behavior Methods ====================

    /**
     * Update application metadata.
     */
    public void updateMetadata(String appName, String description,
            String logoUrl, String homePageUrl) {
        if (appName != null) {
            this.appName = appName;
        }
        if (description != null) {
            this.description = description;
        }
        if (logoUrl != null) {
            this.logoUrl = logoUrl;
        }
        if (homePageUrl != null) {
            this.homePageUrl = homePageUrl;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Update OAuth2 settings.
     */
    public void updateOAuthSettings(Set<String> callbackUrls,
            Set<String> postLogoutRedirectUris,
            Set<String> allowedScopes,
            boolean requirePkce, boolean requireConsent) {
        if (callbackUrls != null) {
            this.callbackUrls = new HashSet<>(callbackUrls);
        }
        if (postLogoutRedirectUris != null) {
            this.postLogoutRedirectUris = new HashSet<>(postLogoutRedirectUris);
        }
        if (allowedScopes != null) {
            this.allowedScopes = new HashSet<>(allowedScopes);
        }
        this.requireProofKey = requirePkce;
        this.requireAuthorizationConsent = requireConsent;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Update token TTL settings.
     */
    public void updateTokenSettings(TokenSettings newSettings) {
        Guard.notNull(newSettings, "Token settings cannot be null");
        this.accessTokenTtlSeconds = newSettings.getAccessTokenTtlSeconds();
        this.refreshTokenTtlSeconds = newSettings.getRefreshTokenTtlSeconds();
        this.updatedAt = LocalDateTime.now();
    }

    // ==================== Query Methods ====================

    public Set<String> getCallbackUrls() {
        if (callbackUrls == null) {
            callbackUrls = new HashSet<>();
        }
        return callbackUrls;
    }

    public Set<String> getPostLogoutRedirectUris() {
        if (postLogoutRedirectUris == null) {
            postLogoutRedirectUris = new HashSet<>();
        }
        return postLogoutRedirectUris;
    }

    public Set<String> getAllowedScopes() {
        if (allowedScopes == null) {
            allowedScopes = new HashSet<>();
        }
        return allowedScopes;
    }

    public boolean isActive() {
        return status == AppStatus.ACTIVE;
    }

    public boolean isBlocked() {
        return status == AppStatus.BLOCKED;
    }
}
