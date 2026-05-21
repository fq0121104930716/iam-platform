package iam.platform.gateway.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import iam.platform.gateway.infrastructure.filter.JwtClaimsHeaderFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for gateway filters.
 *
 * <p>Registers filters that run during request processing to extract JWT claims
 * and add them as standard HTTP headers for downstream services.
 */
@Configuration
public class GatewayFilterConfig {

    /**
     * ObjectMapper bean for serializing JWT claims to JSON.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * JwtClaimsHeaderFilter bean - automatically registered as a WebFilter by Spring.
     * The filter extracts user/tenant information from JWT tokens and adds them
     * as standard HTTP headers for downstream services.
     */
    @Bean
    public JwtClaimsHeaderFilter jwtClaimsHeaderFilter(ObjectMapper objectMapper) {
        return new JwtClaimsHeaderFilter(objectMapper);
    }
}
