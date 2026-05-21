package iam.platform.admin.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import iam.platform.common.util.Guard;
import iam.platform.common.model.valueobject.UserCode;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String userCode;
    private String username;
    private String email;
    private String phone;
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
     * Register a new User with generated UserCode and default states.
     */
    public static User register(String username, String email, String phone,
            String nickname, String avatarUrl) {
        Guard.notBlank(username, "Username cannot be blank");

        return User.builder()
                .userCode(UserCode.generate().getValue())
                .username(username)
                .email(email)
                .phone(phone)
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
     * Enable this user account.
     */
    public void enable() {
        this.enabled = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Disable this user account.
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
