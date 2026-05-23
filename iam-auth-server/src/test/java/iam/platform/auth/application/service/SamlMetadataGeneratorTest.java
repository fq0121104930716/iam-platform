package iam.platform.auth.application.service;

import iam.platform.auth.infrastructure.config.SamlProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for SamlMetadataGenerator.
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class SamlMetadataGeneratorTest {

    private SamlMetadataGenerator metadataGenerator;
    private SamlProperties samlProperties;

    @BeforeAll
    static void initOpenSAML() {
        try {
            log.info("Initializing OpenSAML library");
            InitializationService.initialize();
        } catch (InitializationException e) {
            log.error("Failed to initialize OpenSAML", e);
            throw new RuntimeException("OpenSAML initialization failed", e);
        }
    }

    @BeforeEach
    void setUp() {
        // Create SamlProperties with test configuration
        samlProperties = new SamlProperties();
        samlProperties.setEntityId("https://localhost:9000/saml/metadata");
        samlProperties.setSsoUrl("https://localhost:9000/saml/sso");
        samlProperties.setSignAssertions(true);
        // Use the correct path to the keystore in the project root directory
        String projectRoot = System.getProperty("user.dir").replaceAll("iam-auth-server$", "");
        samlProperties.setSigningKeyPath("file:" + projectRoot + "ssl/keystore.p12");
        samlProperties.setSigningKeyPassword("changeit");

        // Create the generator with test properties
        metadataGenerator = new SamlMetadataGenerator(samlProperties);
    }

    @Test
    void testGenerateMetadata() {
        // When
        String metadataXml = metadataGenerator.generateMetadata();

        // Then
        assertNotNull(metadataXml);
        log.info("Generated SAML Metadata:\n{}", metadataXml);

        // Verify XML declaration
        assertTrue(metadataXml.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));

        // Verify EntityDescriptor (with namespace prefix)
        assertTrue(metadataXml.contains("md:EntityDescriptor")
                || metadataXml.contains("<EntityDescriptor"));
        assertTrue(metadataXml.contains("entityID=\"" + samlProperties.getEntityId() + "\""));

        // Verify IDPSSODescriptor
        assertTrue(metadataXml.contains("md:IDPSSODescriptor")
                || metadataXml.contains("<IDPSSODescriptor"));
        assertTrue(metadataXml
                .contains("WantAuthnRequestsSigned=\"" + samlProperties.isSignAssertions() + "\""));

        // Verify NameID formats are present
        assertTrue(metadataXml.contains("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress"));
        assertTrue(metadataXml.contains("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent"));
        assertTrue(metadataXml.contains("urn:oasis:names:tc:SAML:2.0:nameid-format:transient"));

        // Verify SSO services are present
        assertTrue(metadataXml.contains("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect"));
        assertTrue(metadataXml.contains("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"));
        assertTrue(metadataXml.contains(samlProperties.getSsoUrl()));
    }
}
