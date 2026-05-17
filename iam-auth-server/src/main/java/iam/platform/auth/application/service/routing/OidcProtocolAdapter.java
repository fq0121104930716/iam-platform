package iam.platform.auth.application.service.routing;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * OIDC protocol adapter. Handles routing for OAuth2/OIDC authorization code flow.
 */
@Slf4j
@Component
public class OidcProtocolAdapter implements ProtocolAdapter {

    @Override
    public boolean matches(HttpServletRequest request) {
        // Check if there's a saved OAuth2 authorization request
        String referer = request.getHeader("Referer");
        String requestUri = request.getRequestURI();

        boolean hasOAuth2Referer = referer != null && referer.contains("/oauth2/authorize");
        boolean isOAuth2Callback = requestUri.contains("/oauth2/callback") ||
                requestUri.contains("/login/oauth2/code/");

        return hasOAuth2Referer || isOAuth2Callback;
    }

    @Override
    public ProtocolRoute resolve(ProtocolContext context) {
        String savedRequestUrl = context.getSavedRequestUrl();

        if (savedRequestUrl != null && savedRequestUrl.contains("/oauth2/authorize")) {
            log.debug("OIDC authorization code flow detected, restoring saved request");
            return ProtocolRoute.oidcCode(savedRequestUrl);
        }

        log.debug("OIDC flow without saved request, using default redirect");
        return ProtocolRoute.defaultRedirect(context.getDefaultUrl());
    }
}
