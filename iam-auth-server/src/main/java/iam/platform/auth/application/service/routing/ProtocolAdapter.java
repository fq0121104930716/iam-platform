package iam.platform.auth.application.service.routing;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Protocol adapter interface. Each protocol (OIDC, SAML, CAS) should implement this interface
 * to handle its specific routing logic.
 */
public interface ProtocolAdapter {
    /**
     * Check if this adapter matches the current request.
     */
    boolean matches(HttpServletRequest request);

    /**
     * Resolve the protocol-specific route.
     */
    ProtocolRoute resolve(ProtocolContext context);
}
