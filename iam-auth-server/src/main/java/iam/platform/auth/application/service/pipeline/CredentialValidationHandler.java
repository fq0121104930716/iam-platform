package iam.platform.auth.application.service.pipeline;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import iam.platform.auth.domain.model.valueobject.AuthenticationCredentials;

import java.util.regex.Pattern;

/**
 * Credential validation handler that validates the format of credentials before authentication.
 */
@Slf4j
@Component
public class CredentialValidationHandler implements PreAuthHandler {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$"); // Chinese phone
                                                                                    // number
                                                                                    // pattern

    @Override
    public void handle(PreAuthContext context) {
        AuthenticationCredentials credentials = context.getCredentials();

        if (credentials instanceof AuthenticationCredentials.PasswordCredentials pc) {
            validatePasswordCredentials(pc, context);
        } else if (credentials instanceof AuthenticationCredentials.SmsCodeCredentials sc) {
            validateSmsCodeCredentials(sc, context);
        } else if (credentials instanceof AuthenticationCredentials.EmailCodeCredentials ec) {
            validateEmailCodeCredentials(ec, context);
        } else if (credentials instanceof AuthenticationCredentials.LdapCredentials lc) {
            validateLdapCredentials(lc, context);
        }
        // OAuth2Credentials don't need validation (already validated by provider)
    }

    private void validatePasswordCredentials(AuthenticationCredentials.PasswordCredentials pc,
            PreAuthContext context) {
        if (pc.username() == null || pc.username().isBlank()) {
            throw new PreAuthenticationException("Username is required");
        }
        if (pc.password() == null || pc.password().isBlank()) {
            throw new PreAuthenticationException("Password is required");
        }
        // Set identifier for rate limiting
        context.setIdentifierKey(pc.username());
    }

    private void validateSmsCodeCredentials(AuthenticationCredentials.SmsCodeCredentials sc,
            PreAuthContext context) {
        if (sc.phone() == null || sc.phone().isBlank()) {
            throw new PreAuthenticationException("Phone number is required");
        }
        if (!PHONE_PATTERN.matcher(sc.phone()).matches()) {
            throw new PreAuthenticationException("Invalid phone number format");
        }
        if (sc.code() == null || sc.code().isBlank()) {
            throw new PreAuthenticationException("Verification code is required");
        }
        // Set identifier for rate limiting
        context.setIdentifierKey(sc.phone());
    }

    private void validateEmailCodeCredentials(AuthenticationCredentials.EmailCodeCredentials ec,
            PreAuthContext context) {
        if (ec.email() == null || ec.email().isBlank()) {
            throw new PreAuthenticationException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(ec.email()).matches()) {
            throw new PreAuthenticationException("Invalid email format");
        }
        if (ec.code() == null || ec.code().isBlank()) {
            throw new PreAuthenticationException("Verification code is required");
        }
        // Set identifier for rate limiting
        context.setIdentifierKey(ec.email());
    }

    private void validateLdapCredentials(AuthenticationCredentials.LdapCredentials lc,
            PreAuthContext context) {
        if (lc.username() == null || lc.username().isBlank()) {
            throw new PreAuthenticationException("Username is required");
        }
        if (lc.password() == null || lc.password().isBlank()) {
            throw new PreAuthenticationException("Password is required");
        }
        // Set identifier for rate limiting
        context.setIdentifierKey(lc.username());
    }

    @Override
    public int getOrder() {
        return 30;
    }
}
