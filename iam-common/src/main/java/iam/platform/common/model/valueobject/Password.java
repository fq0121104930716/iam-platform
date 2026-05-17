package iam.platform.common.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.function.BiPredicate;
import java.util.function.UnaryOperator;

/**
 * Value object encapsulating password hashing and policy validation. Uses JDK functional interfaces
 * to avoid Spring Security dependency in common module. Callers pass {@code encoder::encode} and
 * {@code encoder::matches} from PasswordEncoder.
 */
@Getter
@EqualsAndHashCode
public class Password implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int MIN_LENGTH = 8;

    private final String hashedValue;

    private Password(String hashedValue) {
        this.hashedValue = hashedValue;
    }

    /**
     * Create a Password from a raw password string. Validates password policy and hashes the
     * password.
     *
     * @param rawPassword the plain text password
     * @param encodeFn hashing function, e.g. {@code passwordEncoder::encode}
     * @return a new Password value object with the hashed value
     * @throws IllegalArgumentException if the password does not meet policy requirements
     */
    public static Password fromRawPassword(String rawPassword, UnaryOperator<String> encodeFn) {
        validatePolicy(rawPassword);
        String hashed = encodeFn.apply(rawPassword);
        return new Password(hashed);
    }

    /**
     * Reconstitute a Password from a stored hash (e.g., from database). No validation is performed
     * since the hash is already stored.
     */
    public static Password fromHash(String hash) {
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be blank");
        }
        return new Password(hash);
    }

    /**
     * Check if a raw password matches this hashed password.
     *
     * @param rawPassword raw password to check
     * @param matchFn matching function, e.g. {@code passwordEncoder::matches}
     */
    public boolean matches(String rawPassword, BiPredicate<String, String> matchFn) {
        if (rawPassword == null) {
            return false;
        }
        return matchFn.test(rawPassword, this.hashedValue);
    }

    private static void validatePolicy(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must be at least " + MIN_LENGTH + " characters");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException(
                    "Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException(
                    "Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }
    }

    @Override
    public String toString() {
        return "Password[PROTECTED]";
    }
}
