package iam.platform.auth.domain.model.valueobject;

import iam.platform.auth.domain.model.entity.Person;

/**
 * Sealed interface representing authentication input credentials. Each record variant corresponds
 * to a specific authentication method.
 */
public sealed interface AuthenticationCredentials {

    record PasswordCredentials(String username, String password)
            implements AuthenticationCredentials {
        public PasswordCredentials {
            if (username == null || username.isBlank())
                throw new IllegalArgumentException("Username is required");
            if (password == null || password.isBlank())
                throw new IllegalArgumentException("Password is required");
        }
    }

    record SmsCodeCredentials(String phone, String code) implements AuthenticationCredentials {
        public SmsCodeCredentials {
            if (phone == null || phone.isBlank())
                throw new IllegalArgumentException("Phone is required");
            if (code == null || code.isBlank())
                throw new IllegalArgumentException("Verification code is required");
        }
    }

    record EmailCodeCredentials(String email, String code) implements AuthenticationCredentials {
        public EmailCodeCredentials {
            if (email == null || email.isBlank())
                throw new IllegalArgumentException("Email is required");
            if (code == null || code.isBlank())
                throw new IllegalArgumentException("Verification code is required");
        }
    }

    record OAuth2Credentials(String provider, Person resolvedPerson)
            implements AuthenticationCredentials {
        public OAuth2Credentials {
            if (provider == null || provider.isBlank())
                throw new IllegalArgumentException("Provider is required");
            if (resolvedPerson == null)
                throw new IllegalArgumentException("Resolved person is required");
        }
    }

    record LdapCredentials(String username, String password, String domain)
            implements AuthenticationCredentials {
        public LdapCredentials {
            if (username == null || username.isBlank())
                throw new IllegalArgumentException("Username is required");
            if (password == null || password.isBlank())
                throw new IllegalArgumentException("Password is required");
        }
    }
}
