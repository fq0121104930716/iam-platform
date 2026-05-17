package iam.platform.admin.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SSO OIDC Service API")
                        .description("OpenID Connect Authentication Service")
                        .version("1.0.0")
                        .contact(new Contact().name("SSO OIDC Team")));
    }
}
