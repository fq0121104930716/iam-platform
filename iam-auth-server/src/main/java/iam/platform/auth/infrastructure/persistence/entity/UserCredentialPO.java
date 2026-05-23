package iam.platform.auth.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import iam.platform.common.model.enums.CredentialType;
import iam.platform.common.model.enums.CredentialStatus;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_user_credential")
@Getter
@NoArgsConstructor
public class UserCredentialPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "credential_type", nullable = false, length = 30)
    private CredentialType credentialType;

    @Setter
    @Column(name = "credential_value", nullable = false, columnDefinition = "TEXT")
    private String credentialValue;

    @Setter
    @Column(length = 30)
    private String algorithm;

    @Setter
    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary = false;

    @Setter
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Setter
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CredentialStatus status = CredentialStatus.ACTIVE;

    @Setter
    @Column(length = 255)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Setter
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @Setter
    private LocalDateTime updatedAt;

    @Setter
    @Column(name = "created_by", length = 100)
    private String createdBy;
}
