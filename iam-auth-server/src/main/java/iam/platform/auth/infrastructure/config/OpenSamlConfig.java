package iam.platform.auth.infrastructure.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.springframework.context.annotation.Configuration;

/**
 * OpenSAML initialization configuration. Must be initialized before using any OpenSAML
 * functionality.
 */
@Slf4j
@Configuration
public class OpenSamlConfig {

    @PostConstruct
    public void init() {
        try {
            log.info("Initializing OpenSAML library...");
            InitializationService.initialize();
            log.info("OpenSAML initialized successfully");
        } catch (InitializationException e) {
            log.error("Failed to initialize OpenSAML", e);
            throw new RuntimeException("OpenSAML initialization failed", e);
        }
    }
}
