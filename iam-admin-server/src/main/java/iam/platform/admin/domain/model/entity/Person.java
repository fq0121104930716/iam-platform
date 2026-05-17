package iam.platform.admin.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import iam.platform.common.util.Guard;
import iam.platform.common.model.valueobject.Password;
import iam.platform.common.model.valueobject.PersonCode;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    private Long id;
    private String personCode;
    private String username;
    private String email;
    private String phone;
    private String passwordHash;
    private boolean emailVerified;
    private boolean phoneVerified;
    private String nickname;
    private String avatarUrl;
    private boolean enabled;
    private boolean accountLocked;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ==================== Factory Methods ====================

    /**
     * Register a new Person with generated PersonCode and default states.
     */
    public static Person register(String username, String email, String phone,
            Password password, String nickname, String avatarUrl) {
        Guard.notBlank(username, "Username cannot be blank");

        return Person.builder()
                .personCode(PersonCode.generate().getValue())
                .username(username)
                .email(email)
                .phone(phone)
                .passwordHash(password.getHashedValue())
                .emailVerified(false)
                .phoneVerified(false)
                .nickname(nickname)
                .avatarUrl(avatarUrl)
                .enabled(true)
                .accountLocked(false)
                .build();
    }

    // ==================== Behavior Methods ====================

    /**
     * Update profile information (nickname and avatar).
     */
    public void updateProfile(String nickname, String avatarUrl) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (avatarUrl != null) {
            this.avatarUrl = avatarUrl;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Change the email address.
     */
    public void changeEmail(String newEmail) {
        Guard.notBlank(newEmail, "Email cannot be blank");
        this.email = newEmail;
        this.emailVerified = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Change the phone number.
     */
    public void changePhone(String newPhone) {
        Guard.notBlank(newPhone, "Phone cannot be blank");
        this.phone = newPhone;
        this.phoneVerified = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Change the password using a pre-validated and hashed Password value object.
     */
    public void changePassword(Password newPassword) {
        Guard.notNull(newPassword, "Password cannot be null");
        this.passwordHash = newPassword.getHashedValue();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Enable this person account.
     */
    public void enable() {
        this.enabled = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Disable this person account.
     */
    public void disable() {
        this.enabled = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Lock this account (e.g., due to too many failed login attempts).
     */
    public void lock() {
        this.accountLocked = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Unlock this account.
     */
    public void unlock() {
        this.accountLocked = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Record a successful login.
     */
    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Mark email as verified.
     */
    public void markEmailVerified() {
        this.emailVerified = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Mark phone as verified.
     */
    public void markPhoneVerified() {
        this.phoneVerified = true;
        this.updatedAt = LocalDateTime.now();
    }
}
