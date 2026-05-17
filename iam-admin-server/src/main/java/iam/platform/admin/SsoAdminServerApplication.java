package iam.platform.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ConfigurationPropertiesScan("iam.platform.admin.infrastructure.config")
@EnableJpaRepositories("iam.platform.admin.infrastructure.persistence.repository")
public class SsoAdminServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsoAdminServerApplication.class, args);
    }
}
