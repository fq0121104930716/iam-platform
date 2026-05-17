package iam.platform.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class AuditServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditServerApplication.class, args);
    }
}
