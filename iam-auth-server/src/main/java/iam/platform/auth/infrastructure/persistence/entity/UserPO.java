package iam.platform.auth.infrastructure.persistence.entity;

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
@Table(name = "t_user")
@Getter
@NoArgsConstructor
public class UserPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long id;

    @Setter
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Setter
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Setter
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Setter
    @Column(length = 100)
    private String nickname;

    @Setter
    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Setter
    @Column(length = 20)
    private String phone;

    @Setter
    @Column(length = 50)
    private String provider;

    @Setter
    @Column(name = "provider_user_id", length = 255)
    private String providerUserId;

    @Setter
    @Column(name = "phone_verified", nullable = false)
    private boolean phoneVerified = false;

    @Setter
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Setter
    @Column(nullable = false)
    private boolean enabled = true;

    @Setter
    @Column(name = "account_locked", nullable = false)
    private boolean accountLocked = false;

    @Setter
    @Column(name = "user_code", nullable = false, unique = true, length = 50)
    private String userCode;

    @Setter
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @Setter
    private LocalDateTime updatedAt;

    public UserPO(String userCode, String username, String email,
            String nickname) {
        this.userCode = userCode;
        this.username = username;
        this.email = email;
        this.nickname = nickname;
        this.enabled = true;
        this.accountLocked = false;
    }
}
