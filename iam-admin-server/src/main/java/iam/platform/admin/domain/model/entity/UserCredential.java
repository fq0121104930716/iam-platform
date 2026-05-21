package iam.platform.admin.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import iam.platform.common.util.Guard;
import iam.platform.common.model.enums.CredentialType;
import iam.platform.common.model.enums.CredentialStatus;

import java.time.LocalDateTime;
import java.util.function.UnaryOperator;

@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserCredential {
    private Long id;
    private Long userId;
    private CredentialType credentialType;
    private String credentialValue;
    private String algorithm;
    private boolean isPrimary;
    private LocalDateTime expiresAt;
    private LocalDateTime lastUsedAt;
    private CredentialStatus status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;

    // ==================== Factory Methods ====================

    /**
     * Create a password credential from raw password.
     */
    public static UserCredential createPassword(Long userId, String rawPassword, UnaryOperator<String> encoder) {
        Guard.notNull(userId, "User ID cannot be null");
        Guard.notBlank(rawPassword, "Password cannot be blank");
        Guard.notNull(encoder, "Password encoder cannot be null");

        String hashedValue = encoder.apply(rawPassword);

        return UserCredential.builder()
                .userId(userId)
                .credentialType(CredentialType.PASSWORD)
                .credentialValue(hashedValue)
                .algorithm("BCRYPT")
                .isPrimary(true)
                .status(CredentialStatus.ACTIVE)
                .build();
    }

    /**
     * Create a certificate credential from PEM data.
     */
    public static UserCredential createCertificate(Long userId, String pemData) {
        Guard.notNull(userId, "User ID cannot be null");
        Guard.notBlank(pemData, "Certificate PEM data cannot be blank");

        return UserCredential.builder()
                .userId(userId)
                .credentialType(CredentialType.CERTIFICATE)
                .credentialValue(pemData)
                .algorithm("X509")
                .isPrimary(false)
                .status(CredentialStatus.ACTIVE)
                .build();
    }

    // ==================== Behavior Methods ====================

    /**
     * Revoke this credential.
     */
    public void revoke() {
        Guard.state(this.status == CredentialStatus.ACTIVE, "Only ACTIVE credentials can be revoked");
        this.status = CredentialStatus.REVOKED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Mark this credential as expired.
     */
    public void markExpired() {
        this.status = CredentialStatus.EXPIRED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Update last used timestamp.
     */
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if this credential is expired.
     */
    public boolean isExpired() {
        if (this.expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    /**
     * Set or unset this credential as primary.
     */
    public void changePrimary(boolean primary) {
        this.isPrimary = primary;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if this credential is active and not expired.
     */
    public boolean isActive() {
        return this.status == CredentialStatus.ACTIVE && !isExpired();
    }
}
