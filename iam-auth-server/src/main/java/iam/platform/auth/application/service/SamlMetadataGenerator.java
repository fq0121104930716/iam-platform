package iam.platform.auth.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.saml2.metadata.*;
import org.opensaml.security.x509.BasicX509Credential;
import org.springframework.stereotype.Service;
import iam.platform.auth.infrastructure.config.SamlProperties;
import org.w3c.dom.Element;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.time.Instant;
import java.util.Base64;

/**
 * Service for generating SAML 2.0 IdP metadata XML. Generates standard SAML metadata that can be
 * consumed by Service Providers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SamlMetadataGenerator {

        private final SamlProperties samlProperties;
        private final XMLObjectBuilderFactory builderFactory =
                        XMLObjectProviderRegistrySupport.getBuilderFactory();

        @SuppressWarnings("unchecked")
        private <T extends org.opensaml.saml.common.SAMLObject> SAMLObjectBuilder<T> getBuilder(
                        javax.xml.namespace.QName elementName) {
                return (SAMLObjectBuilder<T>) builderFactory.getBuilder(elementName);
        }

        /**
         * Generate SAML 2.0 IdP metadata XML.
         *
         * @return Formatted SAML metadata XML string
         */
        public String generateMetadata() {
                log.info("Generating SAML IdP metadata for entity: {}",
                                samlProperties.getEntityId());

                // Create EntityDescriptor
                SAMLObjectBuilder<EntityDescriptor> entityDescriptorBuilder =
                                getBuilder(EntityDescriptor.DEFAULT_ELEMENT_NAME);
                EntityDescriptor entityDescriptor = entityDescriptorBuilder.buildObject();
                entityDescriptor.setEntityID(samlProperties.getEntityId());
                entityDescriptor.setID("_" + java.util.UUID.randomUUID().toString());
                entityDescriptor.setValidUntil(Instant.now().plusSeconds(365 * 24 * 3600)); // Valid
                                                                                            // for 1
                                                                                            // year
                entityDescriptor.setCacheDuration(java.time.Duration.ofHours(24));

                // Create IDPSSODescriptor
                SAMLObjectBuilder<IDPSSODescriptor> idpDescriptorBuilder =
                                getBuilder(IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
                IDPSSODescriptor idpDescriptor = idpDescriptorBuilder.buildObject();
                idpDescriptor.setWantAuthnRequestsSigned(samlProperties.isSignAssertions());

                // Add protocol support

                // Add NameID formats
                idpDescriptor.getNameIDFormats().add(buildNameIDFormat(
                                "urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress"));
                idpDescriptor.getNameIDFormats().add(buildNameIDFormat(
                                "urn:oasis:names:tc:SAML:2.0:nameid-format:persistent"));
                idpDescriptor.getNameIDFormats().add(buildNameIDFormat(
                                "urn:oasis:names:tc:SAML:2.0:nameid-format:transient"));

                // Add Single Sign-On services with different bindings
                idpDescriptor.getSingleSignOnServices().add(buildSingleSignOnService(
                                "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect",
                                samlProperties.getSsoUrl()));
                idpDescriptor.getSingleSignOnServices()
                                .add(buildSingleSignOnService(
                                                "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST",
                                                samlProperties.getSsoUrl()));

                // Add KeyDescriptor if signing is enabled
                if (samlProperties.isSignAssertions()) {
                        try {
                                KeyDescriptor keyDescriptor = buildKeyDescriptor();
                                if (keyDescriptor != null) {
                                        idpDescriptor.getKeyDescriptors().add(keyDescriptor);
                                        log.info("SAML KeyDescriptor added to metadata");
                                } else {
                                        log.warn("Failed to build KeyDescriptor - certificate not available");
                                }
                        } catch (Exception e) {
                                log.warn("Failed to load certificate for SAML KeyDescriptor: {}",
                                                e.getMessage());
                        }
                }

                entityDescriptor.getRoleDescriptors().add(idpDescriptor);

                // Serialize to XML
                try {
                        Element element = XMLObjectProviderRegistrySupport.getMarshallerFactory()
                                        .getMarshaller(entityDescriptor).marshall(entityDescriptor);

                        String metadataXml = SerializeSupport.prettyPrintXML(element);
                        log.info("SAML IdP metadata generated successfully");
                        return metadataXml;
                } catch (Exception e) {
                        log.error("Failed to generate SAML metadata", e);
                        throw new RuntimeException("Failed to generate SAML metadata", e);
                }
        }

        /**
         * Build NameIDFormat object.
         */
        private NameIDFormat buildNameIDFormat(String format) {
                SAMLObjectBuilder<NameIDFormat> builder =
                                getBuilder(NameIDFormat.DEFAULT_ELEMENT_NAME);
                NameIDFormat nameIDFormat = builder.buildObject();
                nameIDFormat.setURI(format);
                return nameIDFormat;
        }

        /**
         * Build SingleSignOnService object.
         */
        private SingleSignOnService buildSingleSignOnService(String binding, String location) {
                SAMLObjectBuilder<SingleSignOnService> builder =
                                getBuilder(SingleSignOnService.DEFAULT_ELEMENT_NAME);
                SingleSignOnService ssoService = builder.buildObject();
                ssoService.setBinding(binding);
                ssoService.setLocation(location);
                return ssoService;
        }

        /**
         * Build KeyDescriptor with X509 certificate from keystore.
         */
        @SuppressWarnings("unchecked")
        private KeyDescriptor buildKeyDescriptor() {
                try {
                        // Load certificate from keystore
                        java.security.cert.X509Certificate certificate =
                                        loadCertificateFromKeystore();
                        if (certificate == null) {
                                log.warn("No certificate found in keystore");
                                return null;
                        }

                        // Create credential from certificate (for future signing use)
                        @SuppressWarnings("unused")
                        BasicX509Credential credential = new BasicX509Credential(certificate);

                        // Build X509Certificate element
                        XMLObjectBuilder<org.opensaml.xmlsec.signature.X509Certificate> x509CertBuilder =
                                        (XMLObjectBuilder<org.opensaml.xmlsec.signature.X509Certificate>) builderFactory
                                                        .getBuilder(org.opensaml.xmlsec.signature.X509Certificate.DEFAULT_ELEMENT_NAME);
                        org.opensaml.xmlsec.signature.X509Certificate x509Certificate =
                                        x509CertBuilder.buildObject(
                                                        org.opensaml.xmlsec.signature.X509Certificate.DEFAULT_ELEMENT_NAME);

                        // Encode certificate in Base64
                        String certBase64 = Base64.getEncoder()
                                        .encodeToString(certificate.getEncoded());
                        x509Certificate.setValue(certBase64);

                        // Build X509Data element
                        XMLObjectBuilder<org.opensaml.xmlsec.signature.X509Data> x509DataBuilder =
                                        (XMLObjectBuilder<org.opensaml.xmlsec.signature.X509Data>) builderFactory
                                                        .getBuilder(org.opensaml.xmlsec.signature.X509Data.DEFAULT_ELEMENT_NAME);
                        org.opensaml.xmlsec.signature.X509Data x509Data = x509DataBuilder
                                        .buildObject(org.opensaml.xmlsec.signature.X509Data.DEFAULT_ELEMENT_NAME);
                        x509Data.getX509Certificates().add(x509Certificate);

                        // Build KeyInfo element
                        XMLObjectBuilder<org.opensaml.xmlsec.signature.KeyInfo> keyInfoBuilder =
                                        (XMLObjectBuilder<org.opensaml.xmlsec.signature.KeyInfo>) builderFactory
                                                        .getBuilder(org.opensaml.xmlsec.signature.KeyInfo.DEFAULT_ELEMENT_NAME);
                        org.opensaml.xmlsec.signature.KeyInfo keyInfo = keyInfoBuilder.buildObject(
                                        org.opensaml.xmlsec.signature.KeyInfo.DEFAULT_ELEMENT_NAME);
                        keyInfo.getX509Datas().add(x509Data);

                        // Build KeyDescriptor
                        SAMLObjectBuilder<KeyDescriptor> keyDescriptorBuilder =
                                        (SAMLObjectBuilder<KeyDescriptor>) builderFactory
                                                        .getBuilder(KeyDescriptor.DEFAULT_ELEMENT_NAME);
                        KeyDescriptor keyDescriptor = keyDescriptorBuilder.buildObject();
                        keyDescriptor.setUse(org.opensaml.security.credential.UsageType.SIGNING);
                        keyDescriptor.setKeyInfo((org.opensaml.xmlsec.signature.KeyInfo) keyInfo);

                        log.info("Successfully built KeyDescriptor from keystore certificate");
                        return keyDescriptor;

                } catch (Exception e) {
                        log.error("Failed to build KeyDescriptor", e);
                        return null;
                }
        }

        /**
         * Load X509 certificate from keystore file.
         */
        private java.security.cert.X509Certificate loadCertificateFromKeystore() {
                String keyPath = samlProperties.getSigningKeyPath();
                String keyPassword = samlProperties.getSigningKeyPassword();

                if (keyPath == null || keyPath.isEmpty()) {
                        log.warn("SAML signing key path not configured");
                        return null;
                }

                try (InputStream is = resolveResourceAsStream(keyPath)) {
                        KeyStore keyStore = KeyStore.getInstance("PKCS12");
                        char[] password = (keyPassword != null) ? keyPassword.toCharArray()
                                        : new char[0];
                        keyStore.load(is, password);

                        // Get the first certificate from the keystore
                        String alias = keyStore.aliases().nextElement();
                        java.security.cert.Certificate cert = keyStore.getCertificate(alias);

                        if (cert instanceof java.security.cert.X509Certificate) {
                                log.info("Loaded certificate from keystore: {}", keyPath);
                                return (java.security.cert.X509Certificate) cert;
                        } else {
                                log.warn("Certificate in keystore is not X509 format");
                                return null;
                        }
                } catch (Exception e) {
                        log.error("Failed to load certificate from keystore: {}", keyPath, e);
                        return null;
                }
        }

        /**
         * Resolve resource path to InputStream. Supports file: prefix and classpath.
         */
        private InputStream resolveResourceAsStream(String resourcePath) throws Exception {
                if (resourcePath.startsWith("file:")) {
                        String filePath = resourcePath.substring(5);
                        // Handle ${user.dir} placeholder
                        filePath = filePath.replace("${user.dir}", System.getProperty("user.dir"));
                        return new FileInputStream(filePath);
                } else if (resourcePath.startsWith("classpath:")) {
                        String classPath = resourcePath.substring(10);
                        return getClass().getClassLoader().getResourceAsStream(classPath);
                } else {
                        // Assume it's a direct file path
                        return new FileInputStream(resourcePath);
                }
        }


}
