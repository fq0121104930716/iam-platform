package iam.platform.auth.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import iam.platform.auth.application.service.pipeline.PreAuthContext;
import iam.platform.auth.application.service.pipeline.PreAuthenticationPipeline;
import iam.platform.auth.domain.model.entity.User;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;
import iam.platform.auth.domain.model.valueobject.AuthenticationCredentials;
import iam.platform.auth.domain.model.valueobject.UnifiedAuthenticationToken;
import iam.platform.auth.domain.service.AuthenticationStrategy;

import java.util.List;

/**
 * Composite authentication provider that dispatches to the appropriate AuthenticationStrategy based
 * on the credential type in the UnifiedAuthenticationToken.
 *
 * All AuthenticationStrategy beans are auto-discovered via Spring DI.
 */
@Component
@RequiredArgsConstructor
public class CompositeAuthenticationProvider implements AuthenticationProvider {

    private final List<AuthenticationStrategy> strategies;
    private final PreAuthenticationPipeline preAuthenticationPipeline;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        if (!(authentication instanceof UnifiedAuthenticationToken token)) {
            return null;
        }

        AuthenticationCredentials credentials = token.getAuthenticationCredentials();
        if (credentials == null) {
            throw new BadCredentialsException("No credentials provided");
        }

        // Execute pre-authentication pipeline
        PreAuthContext preContext = PreAuthContext.from(credentials, null);
        preAuthenticationPipeline.execute(preContext);

        try {
            // Find matching strategy
            AuthenticationStrategy matchingStrategy =
                    strategies.stream().filter(s -> s.supports(credentials)).findFirst()
                            .orElseThrow(() -> new BadCredentialsException(
                                    "No authentication strategy found for credentials type"));

            // Authenticate using the strategy
            User user = matchingStrategy.authenticate(credentials);
            AuthenticationMethod method = matchingStrategy.getMethod();

            // Record successful authentication
            preAuthenticationPipeline.recordSuccess(preContext);

            // Return authenticated token
            return new UnifiedAuthenticationToken(user, method, List.of());
        } catch (AuthenticationException e) {
            // Record failed authentication
            preAuthenticationPipeline.recordFailure(preContext);
            throw e;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UnifiedAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
