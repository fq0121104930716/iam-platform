package iam.platform.auth.application.service.routing;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;
import iam.platform.auth.domain.model.valueobject.AuthenticationResult;

import java.util.List;

/**
 * Default protocol router implementation. Inspects the saved request URL to determine the source
 * protocol and route accordingly.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProtocolRouterImpl implements ProtocolRouter {

    private final List<ProtocolAdapter> adapters;

    @Override
    public ProtocolRoute resolve(HttpServletRequest request, AuthenticationResult result) {
        // Check if tenant selection is required
        if (result.requiresTenantSelection()) {
            log.debug("Routing to tenant selection");
            return ProtocolRoute.tenantSelection();
        }

        // Get saved request to determine source protocol
        SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, null);
        String savedRequestUrl = savedRequest != null ? savedRequest.getRedirectUrl() : null;
        String defaultUrl = savedRequestUrl;

        ProtocolContext context = new ProtocolContext(result, savedRequestUrl, defaultUrl);

        // Find matching adapter
        for (ProtocolAdapter adapter : adapters) {
            if (adapter.matches(request)) {
                log.debug("Routing with adapter: {}", adapter.getClass().getSimpleName());
                return adapter.resolve(context);
            }
        }

        // Default redirect
        log.debug("No matching adapter found, using default redirect");
        return ProtocolRoute.defaultRedirect(defaultUrl);
    }
}
