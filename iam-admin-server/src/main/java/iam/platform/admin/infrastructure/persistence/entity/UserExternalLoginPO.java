package iam.platform.admin.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_user_external_login")
@Getter
@NoArgsConstructor
public class UserExternalLoginPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long id;

    @Setter
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Setter
    @Column(nullable = false, length = 50)
    private String provider;

    @Setter
    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Setter
    @Column(name = "access_token", columnDefinition = "TEXT")
    private String accessToken;

    @Setter
    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Setter
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Setter
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @Setter
    private LocalDateTime updatedAt;

    public UserExternalLoginPO(Long userId, String provider, String providerUserId) {
        this.userId = userId;
        this.provider = provider;
        this.providerUserId = providerUserId;
    }
}
