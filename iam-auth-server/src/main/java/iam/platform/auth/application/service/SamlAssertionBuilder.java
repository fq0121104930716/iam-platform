package iam.platform.auth.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import iam.platform.auth.domain.model.valueobject.AuthenticationResult;
import iam.platform.auth.infrastructure.config.SamlProperties;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

/**
 * Service for building SAML 2.0 Assertions. Generates XML assertions compliant with SAML 2.0 spec.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SamlAssertionBuilder {

    private final SamlProperties samlProperties;

    /**
     * Build a SAML 2.0 Assertion XML based on the authentication result.
     *
     * @param result the authentication result containing user information
     * @param acsUrl the Assertion Consumer Service URL (SP endpoint)
     * @return Base64-encoded SAML Assertion XML
     */
    public String build(AuthenticationResult result, String acsUrl) {
        String assertionId = "_" + UUID.randomUUID().toString();
        String issueInstant = formatInstant(Instant.now());
        String notBefore = formatInstant(Instant.now().minusSeconds(30));
        String notOnOrAfter = formatInstant(Instant.now().plusSeconds(
                samlProperties.getAssertionValidityMinutes() * 60L));

        String nameId = resolveNameId(result);
        String authnInstant = formatInstant(result.authenticatedAt().toInstant(
                java.time.ZoneOffset.UTC));

        String assertionXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <saml2p:Response xmlns:saml2p="urn:oasis:names:tc:SAML:2.0:protocol"
                                 ID="%s"
                                 Version="2.0"
                                 IssueInstant="%s"
                                 Destination="%s">
                    <saml2:Issuer xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion">%s</saml2:Issuer>
                    <saml2p:Status>
                        <saml2p:StatusCode Value="urn:oasis:names:tc:SAML:2.0:status:Success"/>
                    </saml2p:Status>
                    <saml2:Assertion xmlns:saml2="urn:oasis:names:tc:SAML:2.0:assertion"
                                     ID="%s"
                                     IssueInstant="%s"
                                     Version="2.0">
                        <saml2:Issuer>%s</saml2:Issuer>
                        <saml2:Subject>
                            <saml2:NameID Format="%s">%s</saml2:NameID>
                            <saml2:SubjectConfirmation Method="urn:oasis:names:tc:SAML:2.0:cm:bearer">
                                <saml2:SubjectConfirmationData NotOnOrAfter="%s"
                                                               Recipient="%s"/>
                            </saml2:SubjectConfirmation>
                        </saml2:Subject>
                        <saml2:Conditions NotBefore="%s" NotOnOrAfter="%s">
                            <saml2:AudienceRestriction>
                                <saml2:Audience>%s</saml2:Audience>
                            </saml2:AudienceRestriction>
                        </saml2:Conditions>
                        <saml2:AuthnStatement AuthnInstant="%s"
                                              SessionIndex="%s">
                            <saml2:AuthnContext>
                                <saml2:AuthnContextClassRef>urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport</saml2:AuthnContextClassRef>
                            </saml2:AuthnContext>
                        </saml2:AuthnStatement>
                        <saml2:AttributeStatement>
                            <saml2:Attribute Name="email" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:basic">
                                <saml2:AttributeValue xsi:type="xs:string">%s</saml2:AttributeValue>
                            </saml2:Attribute>
                            <saml2:Attribute Name="nickname" NameFormat="urn:oasis:names:tc:SAML:2.0:attrname-format:basic">
                                <saml2:AttributeValue xsi:type="xs:string">%s</saml2:AttributeValue>
                            </saml2:Attribute>
                        </saml2:AttributeStatement>
                    </saml2:Assertion>
                </saml2p:Response>
                """.formatted(
                assertionId, issueInstant, acsUrl, samlProperties.getEntityId(),
                assertionId, issueInstant, samlProperties.getEntityId(),
                samlProperties.getNameIdFormat(), nameId,
                notOnOrAfter, acsUrl,
                notBefore, notOnOrAfter, acsUrl,
                authnInstant, assertionId,
                result.person().getEmail() != null ? result.person().getEmail() : "",
                result.person().getNickname() != null ? result.person().getNickname() : ""
        );

        // Base64 encode the assertion
        return Base64.getEncoder().encodeToString(assertionXml.getBytes());
    }

    private String resolveNameId(AuthenticationResult result) {
        // Use email if available, otherwise use username
        String nameId = result.person().getEmail();
        if (nameId == null || nameId.isBlank()) {
            nameId = result.person().getUsername();
        }
        return nameId;
    }

    private String formatInstant(Instant instant) {
        return DateTimeFormatter.ISO_INSTANT.format(instant.atZone(ZoneOffset.UTC));
    }
}
