package iam.platform.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validation annotation for credential type.
 * 
 * <p>
 * Ensures that the credential type is a valid value from the CredentialType enum.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ValidCredentialTypeValidator.class)
public @interface ValidCredentialType {
    String message() default "Invalid credential type. Must be one of: PASSWORD, CERTIFICATE";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
