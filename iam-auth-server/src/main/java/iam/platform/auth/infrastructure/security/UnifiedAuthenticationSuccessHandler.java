package iam.platform.auth.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import iam.platform.auth.application.service.AuthenticationApplicationService;
import iam.platform.auth.application.service.routing.ProtocolRoute;
import iam.platform.auth.application.service.routing.ProtocolRouter;
import iam.platform.auth.domain.model.entity.User;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;
import iam.platform.auth.domain.model.valueobject.AuthenticationResult;
import iam.platform.auth.domain.service.impl.OAuth2AuthenticationStrategy;

import java.io.IOException;

/**
 * The convergence point for ALL authentication methods (first-party and OAuth2).
 *
 * After credential validation by any strategy, this handler runs the post-authentication pipeline
 * and delegates to ProtocolRouter to determine the appropriate redirect destination.
 */
@Slf4j
@RequiredArgsConstructor
public class UnifiedAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthenticationApplicationService applicationService;
    private final ProtocolRouter protocolRouter;

    private static final String DEFAULT_TARGET_URL = "/";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        User user;
        AuthenticationMethod method;

        if (authentication instanceof UnifiedAuthenticationToken unifiedToken) {
            // First-party authentication (password, SMS, email, LDAP)
            user = unifiedToken.getUser();
            method = unifiedToken.getMethod();
        } else if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            // OAuth2 social login
            CustomOAuth2User oauth2User = (CustomOAuth2User) oauth2Token.getPrincipal();
            user = oauth2User.getUser();
            String provider = oauth2Token.getAuthorizedClientRegistrationId();
            method = OAuth2AuthenticationStrategy.fromProvider(provider);
        } else {
            log.warn("Unknown authentication type: {}", authentication.getClass().getName());
            response.sendRedirect(DEFAULT_TARGET_URL);
            return;
        }

        // Run the post-authentication pipeline
        AuthenticationResult result =
                applicationService.completeAuthentication(user, method, request);

        // Delegate to protocol router to determine redirect destination
        ProtocolRoute route = protocolRouter.resolve(request, result);
        log.debug("Protocol routing decision: {} -> {}", route.type(), route.redirectUrl());

        response.sendRedirect(route.redirectUrl());
    }
}
