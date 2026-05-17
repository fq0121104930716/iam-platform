package iam.platform.auth.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "security.jwk.rsa")
public class JwkProperties {
    private String privateKeyLocation;
    private String publicKeyLocation;
}
