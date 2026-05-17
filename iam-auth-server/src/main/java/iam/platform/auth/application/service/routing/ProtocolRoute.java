package iam.platform.auth.application.service.routing;

import java.util.Map;

/**
 * Route result object that contains the routing decision and target URL.
 */
public record ProtocolRoute(
        RouteType type,
        String redirectUrl,
        Map<String, String> additionalParams
) {
    public enum RouteType {
        /** OIDC authorization code flow */
        OIDC_CODE,
        /** SAML assertion */
        SAML_ASSERTION,
        /** CAS ticket */
        CAS_TICKET,
        /** API token response */
        API_TOKEN,
        /** Tenant selection required */
        TENANT_SELECTION,
        /** Default redirect (homepage) */
        DEFAULT_REDIRECT
    }

    /**
     * Create a tenant selection route.
     */
    public static ProtocolRoute tenantSelection() {
        return new ProtocolRoute(RouteType.TENANT_SELECTION, "/select-tenant", Map.of());
    }

    /**
     * Create a default redirect route.
     */
    public static ProtocolRoute defaultRedirect(String defaultUrl) {
        return new ProtocolRoute(RouteType.DEFAULT_REDIRECT, defaultUrl != null ? defaultUrl : "/", Map.of());
    }

    /**
     * Create an OIDC code route (restore saved OAuth2 request).
     */
    public static ProtocolRoute oidcCode(String savedRequestUrl) {
        return new ProtocolRoute(RouteType.OIDC_CODE, savedRequestUrl, Map.of());
    }

    /**
     * Create a SAML assertion route.
     * @param assertionXml the Base64-encoded SAML Assertion XML
     * @param acsUrl the Assertion Consumer Service URL
     * @param relayState the SAML RelayState parameter
     */
    public static ProtocolRoute samlAssertion(String assertionXml, String acsUrl, String relayState) {
        return new ProtocolRoute(RouteType.SAML_ASSERTION, acsUrl, Map.of(
                "assertion", assertionXml,
                "relayState", relayState != null ? relayState : ""
        ));
    }

    /**
     * Create a CAS ticket route.
     * @param ticket the CAS Service Ticket
     * @param service the CAS service URL
     */
    public static ProtocolRoute casTicket(String ticket, String service) {
        return new ProtocolRoute(RouteType.CAS_TICKET, service + "?ticket=" + ticket, Map.of(
                "ticket", ticket,
                "service", service
        ));
    }
}
