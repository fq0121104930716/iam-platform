package iam.platform.common.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Value object representing a Person's unique code.
 * Format: "PERSON-" followed by 8 uppercase alphanumeric characters.
 */
@Getter
@EqualsAndHashCode
public class PersonCode implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Pattern CODE_PATTERN = Pattern.compile("^PERSON-[A-Z0-9]{8}$");

    private final String value;

    private PersonCode(String value) {
        this.value = value;
    }

    /**
     * Generate a new unique PersonCode.
     */
    public static PersonCode generate() {
        String code = "PERSON-" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 8).toUpperCase();
        return new PersonCode(code);
    }

    /**
     * Reconstitute a PersonCode from a stored value (e.g., from database).
     */
    public static PersonCode of(String raw) {
        if (raw == null || !CODE_PATTERN.matcher(raw).matches()) {
            throw new IllegalArgumentException("Invalid person code format: " + raw);
        }
        return new PersonCode(raw);
    }

    @Override
    public String toString() {
        return value;
    }
}
