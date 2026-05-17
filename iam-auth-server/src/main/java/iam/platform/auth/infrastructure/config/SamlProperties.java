package iam.platform.auth.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * SAML 2.0 IdP configuration properties.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "sso.saml")
public class SamlProperties {

    /** SAML IdP entity ID */
    private String entityId = "https://sso.example.com/saml/metadata";

    /** SAML IdP SSO endpoint URL */
    private String ssoUrl = "https://sso.example.com/saml/sso";

    /** SAML assertion validity duration in minutes */
    private int assertionValidityMinutes = 5;

    /** SAML signature algorithm */
    private String signatureAlgorithm = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";

    /** SAML name ID format */
    private String nameIdFormat = "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress";

    /** Whether to sign SAML assertions */
    private boolean signAssertions = true;

    /** Path to signing key file (PKCS#12 or JKS) */
    private String signingKeyPath;

    /** Signing key password */
    private String signingKeyPassword;
}
