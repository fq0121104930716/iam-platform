package iam.platform.auth.application.service;

import iam.platform.auth.application.service.pipeline.PreAuthContext;
import iam.platform.auth.application.service.pipeline.PreAuthenticationPipeline;
import iam.platform.auth.domain.model.entity.User;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;
import iam.platform.auth.domain.model.valueobject.AuthenticationCredentials;
import iam.platform.auth.domain.model.valueobject.UnifiedAuthenticationToken;
import iam.platform.auth.domain.service.AuthenticationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Composite authentication provider that dispatches to the appropriate AuthenticationStrategy based
 * on the credential type in the UnifiedAuthenticationToken.
 * 
 * REFACTORED: Changed from direct List injection to ObjectProvider to eliminate circular dependencies.
 * ObjectProvider defers bean resolution until first use, breaking the cycle at construction time.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompositeAuthenticationProvider implements AuthenticationProvider {

    private final ObjectProvider<List<AuthenticationStrategy>> strategiesProvider;
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
            List<AuthenticationStrategy> strategies = strategiesProvider.getIfAvailable(List::of);
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
