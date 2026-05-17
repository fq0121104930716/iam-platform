package iam.platform.auth.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import iam.platform.common.util.Guard;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonExternalLogin {
    private Long id;
    private Long personId;
    private String provider;
    private String providerUserId;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime expiresAt;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Factory Methods ====================

    /**
     * Link an external login provider to a person.
     */
    public static PersonExternalLogin link(Long personId, String provider,
            String providerUserId, String accessToken, String refreshToken, LocalDateTime expiresAt) {
        Guard.notNull(personId, "Person ID cannot be null");
        Guard.positive(personId.intValue(), "Person ID must be positive");
        Guard.notBlank(provider, "Provider cannot be blank");
        Guard.notBlank(providerUserId, "Provider user ID cannot be blank");
        Guard.notBlank(accessToken, "Access token cannot be blank");

        LocalDateTime now = LocalDateTime.now();
        return PersonExternalLogin.builder()
                .personId(personId)
                .provider(provider)
                .providerUserId(providerUserId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresAt(expiresAt)
                .lastUsedAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    // ==================== Behavior Methods ====================

    public void updateTokens(String accessToken, String refreshToken, LocalDateTime expiresAt) {
        Guard.notBlank(accessToken, "Access token cannot be blank");
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.updatedAt = LocalDateTime.now();
    }

    public void recordUsage() {
        this.lastUsedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt != null && expiresAt.isBefore(now);
    }

    public boolean canRefresh(LocalDateTime now) {
        return refreshToken != null && !isExpired(now);
    }
}
