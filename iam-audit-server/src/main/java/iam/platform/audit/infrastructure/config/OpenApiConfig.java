package iam.platform.audit.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for audit server.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI auditOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IAM Audit Server API")
                        .description("Centralized Audit Log Management Service")
                        .version("1.2.0")
                        .contact(new Contact()
                                .name("IAM Platform Team")
                                .email("admin@example.com")));
    }
}
