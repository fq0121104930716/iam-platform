package iam.platform.auth.application.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import iam.platform.auth.infrastructure.config.SamlProperties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for SamlMetadataGenerator.
 */
@Slf4j
@SpringBootTest
class SamlMetadataGeneratorTest {

    @Autowired
    private SamlMetadataGenerator metadataGenerator;

    @Autowired
    private SamlProperties samlProperties;

    @Test
    void testGenerateMetadata() {
        // When
        String metadataXml = metadataGenerator.generateMetadata();

        // Then
        assertNotNull(metadataXml);
        assertTrue(metadataXml.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(metadataXml.contains("<EntityDescriptor"));
        assertTrue(metadataXml.contains("entityID=\"" + samlProperties.getEntityId() + "\""));
        assertTrue(metadataXml.contains("<IDPSSODescriptor"));
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

        log.info("Generated SAML Metadata:\n{}", metadataXml);
    }
}
