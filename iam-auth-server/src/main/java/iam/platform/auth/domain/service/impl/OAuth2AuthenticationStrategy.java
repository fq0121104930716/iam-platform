package iam.platform.auth.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import iam.platform.auth.domain.model.entity.Person;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;
import iam.platform.auth.domain.model.valueobject.AuthenticationCredentials;
import iam.platform.auth.domain.service.AuthenticationStrategy;

/**
 * Strategy for OAuth2 social login. Credential validation is handled externally by the OAuth2
 * provider. This strategy exists to provide symmetry in the pipeline and map the provider to the
 * correct method.
 */
@Service
@RequiredArgsConstructor
public class OAuth2AuthenticationStrategy implements AuthenticationStrategy {

    @Override
    public AuthenticationMethod getMethod() {
        // Will be overridden dynamically based on provider; this is a placeholder
        return AuthenticationMethod.OAUTH2_DINGTALK;
    }

    @Override
    public boolean supports(AuthenticationCredentials credentials) {
        return credentials instanceof AuthenticationCredentials.OAuth2Credentials;
    }

    @Override
    public Person authenticate(AuthenticationCredentials credentials) {
        // OAuth2 credentials have already been validated by the provider.
        // The Person was resolved by CustomOAuth2UserService before reaching here.
        AuthenticationCredentials.OAuth2Credentials oc =
                (AuthenticationCredentials.OAuth2Credentials) credentials;
        return oc.resolvedPerson();
    }

    @Override
    public boolean isRedirectBased() {
        return true;
    }

    /**
     * Map OAuth2 provider registration ID to AuthenticationMethod.
     */
    public static AuthenticationMethod fromProvider(String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "dingtalk" -> AuthenticationMethod.OAUTH2_DINGTALK;
            case "wecom", "wechat" -> AuthenticationMethod.OAUTH2_WECOM;
            default -> AuthenticationMethod.OAUTH2_DINGTALK;
        };
    }
}
