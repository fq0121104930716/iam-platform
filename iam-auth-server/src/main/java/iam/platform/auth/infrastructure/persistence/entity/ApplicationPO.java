package iam.platform.auth.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import iam.platform.auth.infrastructure.persistence.converter.EncryptedStringConverter;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_application")
@Getter
@NoArgsConstructor
public class ApplicationPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long id;

    @Setter
    @Column(name = "app_id", nullable = false, unique = true, length = 100)
    private String appId;

    @Setter
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "app_secret", length = 500)
    private String appSecret;

    @Setter
    @Column(name = "app_name", nullable = false, length = 200)
    private String appName;

    @Setter
    @Column(name = "tenant_id")
    private Long tenantId;

    @Setter
    @Column(name = "app_type", nullable = false, length = 20)
    private String appType;

    @Setter
    @Column(length = 500)
    private String description;

    @Setter
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Setter
    @Column(nullable = false, length = 20)
    private String status;

    @Setter
    @Column(name = "home_page_url", length = 500)
    private String homePageUrl;

    @Setter
    @Column(name = "callback_urls", length = 2000)
    private String callbackUrls;

    @Setter
    @Column(name = "post_logout_redirect_uris", length = 2000)
    private String postLogoutRedirectUris;

    @Setter
    @Column(name = "allowed_scopes", nullable = false, length = 1000)
    private String allowedScopes;

    @Setter
    @Column(name = "require_proof_key", nullable = false)
    private boolean requireProofKey;

    @Setter
    @Column(name = "require_authorization_consent", nullable = false)
    private boolean requireAuthorizationConsent;

    @Setter
    @Column(name = "access_token_ttl_seconds", nullable = false)
    private int accessTokenTtlSeconds;

    @Setter
    @Column(name = "refresh_token_ttl_seconds", nullable = false)
    private int refreshTokenTtlSeconds;

    @Setter
    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @Setter
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
