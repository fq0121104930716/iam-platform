package iam.platform.auth.application.service.routing;

import jakarta.servlet.http.HttpServletRequest;
import iam.platform.auth.domain.model.valueobject.AuthenticationResult;

/**
 * Protocol router interface. Resolves the appropriate route based on the source protocol.
 */
public interface ProtocolRouter {
    /**
     * Resolve the route based on the source protocol.
     *
     * @param request the HTTP request
     * @param result the authentication result
     * @return the resolved protocol route
     */
    ProtocolRoute resolve(HttpServletRequest request, AuthenticationResult result);
}
