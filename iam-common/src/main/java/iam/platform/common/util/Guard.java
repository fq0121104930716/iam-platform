package iam.platform.common.util;

import iam.platform.common.model.exception.InvalidStateException;

/**
 * Domain assertion utility for enforcing preconditions and invariants.
 */
public final class Guard {

    private Guard() {
    }

    public static void notNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void state(boolean expression, String message) {
        if (!expression) {
            throw new InvalidStateException(message);
        }
    }

    public static void positive(int value, String message) {
        if (value <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
