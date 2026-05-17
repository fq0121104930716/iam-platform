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
@Table(name = "t_person")
@Getter
@NoArgsConstructor
public class PersonPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter
    private Long id;

    @Setter
    @Column(name = "person_code", nullable = false, unique = true, length = 50)
    private String personCode;

    @Setter
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Setter
    @Column(length = 255)
    private String email;

    @Setter
    @Column(length = 20)
    private String phone;

    @Setter
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Setter
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Setter
    @Column(name = "phone_verified", nullable = false)
    private boolean phoneVerified = false;

    @Setter
    @Column(length = 100)
    private String nickname;

    @Setter
    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Setter
    @Column(nullable = false)
    private boolean enabled = true;

    @Setter
    @Column(name = "account_locked", nullable = false)
    private boolean accountLocked = false;

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

    public PersonPO(String personCode, String username, String email, String passwordHash,
            String nickname) {
        this.personCode = personCode;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.enabled = true;
        this.accountLocked = false;
    }
}
