package iam.platform.auth.application.service.routing;

import lombok.Getter;
import iam.platform.auth.domain.model.valueobject.AuthenticationResult;

/**
 * Context object for protocol routing decisions.
 */
@Getter
public class ProtocolContext {
    private final AuthenticationResult authenticationResult;
    private final String savedRequestUrl;
    private final String defaultUrl;

    public ProtocolContext(AuthenticationResult authenticationResult, String savedRequestUrl, String defaultUrl) {
        this.authenticationResult = authenticationResult;
        this.savedRequestUrl = savedRequestUrl;
        this.defaultUrl = defaultUrl;
    }
}
