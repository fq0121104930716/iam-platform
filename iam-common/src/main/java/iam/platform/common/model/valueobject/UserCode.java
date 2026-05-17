package iam.platform.common.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Value object representing a User's unique code.
 * Format: "USER-" followed by 8 uppercase alphanumeric characters.
 */
@Getter
@EqualsAndHashCode
public class UserCode implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Pattern CODE_PATTERN = Pattern.compile("^USER-[A-Z0-9]{8}$");

    private final String value;

    private UserCode(String value) {
        this.value = value;
    }

    /**
     * Generate a new unique UserCode.
     */
    public static UserCode generate() {
        String code = "USER-" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 8).toUpperCase();
        return new UserCode(code);
    }

    /**
     * Reconstitute a UserCode from a stored value (e.g., from database).
     */
    public static UserCode of(String raw) {
        if (raw == null || !CODE_PATTERN.matcher(raw).matches()) {
            throw new IllegalArgumentException("Invalid user code format: " + raw);
        }
        return new UserCode(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
