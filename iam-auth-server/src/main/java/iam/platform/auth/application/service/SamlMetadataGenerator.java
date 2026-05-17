package iam.platform.auth.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.common.SAMLObjectBuilder;
import org.opensaml.saml.saml2.metadata.*;
import org.springframework.stereotype.Service;
import iam.platform.auth.infrastructure.config.SamlProperties;
import org.w3c.dom.Element;

import java.time.Instant;

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
                        // TODO: Implement actual certificate loading from keystore
                        // For now, skip adding KeyDescriptor to avoid compilation errors
                        log.warn("SAML assertion signing is enabled but certificate not configured");
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


}
