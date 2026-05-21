package iam.platform.common.validation;

import iam.platform.common.model.enums.CredentialType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for credential type.
 * 
 * <p>
 * Validates that the credential type string matches one of the valid enum values.
 */
public class ValidCredentialTypeValidator
        implements ConstraintValidator<ValidCredentialType, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null is handled by @NotBlank or @NotNull
        if (value == null || value.isBlank()) {
            return true;
        }

        try {
            CredentialType.valueOf(value.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
