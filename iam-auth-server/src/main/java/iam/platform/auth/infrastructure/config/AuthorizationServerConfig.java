package iam.platform.auth.infrastructure.config;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;

import iam.platform.auth.infrastructure.security.TenantAwareAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class AuthorizationServerConfig {

        private final ResourceLoader resourceLoader;
        private final JwkProperties jwkProperties;
        private final TenantAwareAuthenticationFilter tenantAwareAuthenticationFilter;

        @Bean
        @Order(1)
        public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
                        throws Exception {
                OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

                http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                                .oidc(Customizer.withDefaults());

                http.exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));

                http.oauth2ResourceServer(rs -> rs.jwt(Customizer.withDefaults()));

                // Add tenant-aware authentication filter to the authorization server chain
                http.addFilterAfter(tenantAwareAuthenticationFilter,
                                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public JWKSource<SecurityContext> jwkSource() throws Exception {
                Resource privateKeyResource =
                                resourceLoader.getResource(jwkProperties.getPrivateKeyLocation());
                Resource publicKeyResource =
                                resourceLoader.getResource(jwkProperties.getPublicKeyLocation());

                String privateKeyPem =
                                privateKeyResource.getContentAsString(StandardCharsets.UTF_8);
                String publicKeyPem = publicKeyResource.getContentAsString(StandardCharsets.UTF_8);

                RSAPrivateKey privateKey = parseRsaPrivateKey(privateKeyPem);
                RSAPublicKey publicKey = parseRsaPublicKey(publicKeyPem);

                // 使用公钥指纹作为 KeyID，确保重启后保持一致
                String keyId = generateKeyId(publicKey);

                RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(keyId)
                                .build();

                JWKSet jwkSet = new JWKSet(rsaKey);
                return new ImmutableJWKSet<>(jwkSet);
        }

        @Bean
        public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
                return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
        }

        @Bean
        public AuthorizationServerSettings authorizationServerSettings(
                        @Value("${security.issuer-uri:http://localhost:9000}") String issuerUri) {
                return AuthorizationServerSettings.builder().issuer(issuerUri).build();
        }

        private RSAPrivateKey parseRsaPrivateKey(String pem) throws Exception {
                String content = pem.replaceAll("-----.*?-----", "").replaceAll("\\s+", "");
                byte[] decoded = Base64.getDecoder().decode(content);
                PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        }

        private RSAPublicKey parseRsaPublicKey(String pem) throws Exception {
                String content = pem.replaceAll("-----.*?-----", "").replaceAll("\\s+", "");
                byte[] decoded = Base64.getDecoder().decode(content);
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        }

        /**
         * 基于公钥内容生成稳定的 KeyID（使用 SHA-256 指纹前 16 位） 确保应用重启后 KeyID 保持一致，避免客户端 JWKS 缓存失效
         */
        private String generateKeyId(RSAPublicKey publicKey) throws Exception {
                byte[] encoded = publicKey.getEncoded();
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(encoded);
                // 取前 16 位（128 位）作为 KeyID
                byte[] keyIdBytes = new byte[16];
                System.arraycopy(hash, 0, keyIdBytes, 0, 16);
                return Base64.getUrlEncoder().withoutPadding().encodeToString(keyIdBytes);
        }
}
