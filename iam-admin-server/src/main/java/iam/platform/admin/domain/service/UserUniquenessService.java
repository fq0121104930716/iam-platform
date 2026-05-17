package iam.platform.admin.domain.service;

/**
 * Domain service for ensuring User field uniqueness.
 * This logic requires repository access, so it cannot reside in the entity.
 */
public interface UserUniquenessService {

    /**
     * Ensure the username is not already taken.
     *
     * @throws iam.platform.common.model.exception.ConflictException if username exists
     */
    void ensureUsernameUnique(String username);

    /**
     * Ensure the email is not already taken by another user.
     *
     * @param email         the email to check
     * @param excludeUserId user ID to exclude (for updates), null for new creation
     * @throws iam.platform.common.model.exception.ConflictException if email exists
     */
    void ensureEmailUnique(String email, Long excludeUserId);

    /**
     * Ensure the phone is not already taken by another user.
     *
     * @param phone         the phone to check
     * @param excludeUserId user ID to exclude (for updates), null for new creation
     * @throws iam.platform.common.model.exception.ConflictException if phone exists
     */
    void ensurePhoneUnique(String phone, Long excludeUserId);
}
