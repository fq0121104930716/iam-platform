package iam.platform.auth.application.service.routing;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import iam.platform.auth.application.service.SamlAssertionBuilder;

/**
 * SAML 2.0 protocol adapter. Handles routing for SAML SSO requests.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SamlProtocolAdapter implements ProtocolAdapter {

    private final SamlAssertionBuilder assertionBuilder;

    @Override
    public boolean matches(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        return requestUri.contains("/saml/") || requestUri.contains("/sso/saml");
    }

    @Override
    public ProtocolRoute resolve(ProtocolContext context) {
        // SAML requires ACS URL and RelayState from the original request
        String acsUrl = context.getSavedRequestUrl();
        
        if (acsUrl == null || acsUrl.isBlank()) {
            log.warn("SAML ACS URL not found in context, falling back to default redirect");
            return ProtocolRoute.defaultRedirect(context.getDefaultUrl());
        }

        // Extract RelayState and SAML Request ID from context (set by controller)
        String relayState = context.getAuthenticationResult() != null 
                ? context.getAuthenticationResult().user().getUserCode() 
                : "";
        
        // Generate SAML Assertion
        String assertionXml = assertionBuilder.build(context.getAuthenticationResult(), acsUrl);
        
        log.debug("SAML assertion generated for ACS: {}", acsUrl);
        return ProtocolRoute.samlAssertion(assertionXml, acsUrl, relayState);
    }
}
