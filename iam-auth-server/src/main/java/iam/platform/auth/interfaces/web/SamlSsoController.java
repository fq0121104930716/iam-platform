package iam.platform.auth.interfaces.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import iam.platform.auth.application.service.SamlAssertionBuilder;
import iam.platform.auth.domain.model.entity.Person;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;
import iam.platform.auth.domain.model.valueobject.AuthenticationResult;
import iam.platform.auth.domain.repository.PersonRepository;

import java.util.List;
import java.util.Set;

/**
 * SAML 2.0 SSO Controller. Handles SAML authentication requests and generates SAML assertions.
 */
@Slf4j
@Controller
@RequestMapping("/saml")
@RequiredArgsConstructor
public class SamlSsoController {

    private final SamlAssertionBuilder assertionBuilder;
    private final PersonRepository personRepository;

    /**
     * SAML SSO endpoint - displays login page if user is not authenticated. GET
     * /saml/sso?acsUrl=https://sp.example.com/acs&relayState=xyz
     */
    @GetMapping("/sso")
    public String samlSsoLogin(@RequestParam String acsUrl,
            @RequestParam(required = false) String relayState, Model model) {

        model.addAttribute("acsUrl", acsUrl);
        model.addAttribute("relayState", relayState);
        model.addAttribute("loginType", "saml");

        log.debug("SAML SSO login page requested, ACS: {}", acsUrl);
        return "saml-login";
    }

    /**
     * SAML SSO processing endpoint - authenticates user and generates SAML assertion. POST
     * /saml/sso
     */
    @PostMapping("/sso")
    public void processSamlSso(@RequestParam String username, @RequestParam String password,
            @RequestParam String acsUrl, @RequestParam(required = false) String relayState,
            HttpServletRequest request, HttpServletResponse response) throws Exception {

        log.info("Processing SAML SSO request for user: {}", username);

        // 1. Authenticate the user (simplified - in real implementation, use
        // AuthenticationDispatcher)
        Person person = authenticateUser(username, password);

        if (person == null) {
            // Authentication failed, redirect back to login with error
            response.sendRedirect("/saml/sso?acsUrl=" + java.net.URLEncoder.encode(acsUrl, "UTF-8")
                    + "&error=invalid_credentials");
            return;
        }

        // 2. Create authentication result
        AuthenticationResult authResult =
                AuthenticationResult.withSelectedTenant(person, AuthenticationMethod.PASSWORD, null, // No
                                                                                                     // tenant
                                                                                                     // selection
                                                                                                     // for
                                                                                                     // SAML
                        List.of(), Set.of());

        // 3. Generate SAML Assertion
        String assertionXml = assertionBuilder.build(authResult, acsUrl);

        // 4. Return auto-submit form to SP's ACS endpoint
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(buildAutoSubmitForm(acsUrl, assertionXml, relayState));

        log.info("SAML SSO successful for user: {}, ACS: {}", username, acsUrl);
    }

    /**
     * SAML Metadata endpoint - provides IdP metadata for SP configuration. GET /saml/metadata
     */
    @GetMapping("/metadata")
    public String samlMetadata(Model model) {
        // TODO: Implement SAML metadata generation
        model.addAttribute("entityId", "https://sso.example.com/saml/metadata");
        model.addAttribute("ssoUrl", "https://sso.example.com/saml/sso");
        return "saml-metadata";
    }

    /**
     * Authenticate user (simplified implementation). In production, this should delegate to
     * AuthenticationDispatcher.
     */
    private Person authenticateUser(String username, String password) {
        // Simplified: lookup user by username
        // In real implementation, use AuthenticationDispatcher.authenticate()
        return personRepository.findByUsername(username).orElse(null);
    }

    /**
     * Build HTML auto-submit form to send SAML Response to SP's ACS.
     */
    private String buildAutoSubmitForm(String acsUrl, String assertionXml, String relayState) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>SAML SSO</title>
                    <script type="text/javascript">
                        function submitForm() {
                            document.getElementById('samlForm').submit();
                        }
                        window.onload = submitForm;
                    </script>
                </head>
                <body>
                    <noscript>
                        <p>Your browser does not support JavaScript.
                        <a href="%s" target="_blank">Continue</a></p>
                    </noscript>
                    <form id="samlForm" action="%s" method="POST">
                        <input type="hidden" name="SAMLResponse" value="%s"/>
                        %s
                        <input type="submit" value="Continue"/>
                    </form>
                </body>
                </html>
                """.formatted(acsUrl, acsUrl, assertionXml,
                relayState != null
                        ? "<input type=\"hidden\" name=\"RelayState\" value=\"" + relayState
                                + "\"/>"
                        : "");
    }
}
