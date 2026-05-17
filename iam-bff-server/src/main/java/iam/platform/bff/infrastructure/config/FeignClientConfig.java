package iam.platform.bff.infrastructure.config;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Feign clients.
 */
@Configuration
public class FeignClientConfig {

    /**
     * Add common headers to Feign requests.
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Add any common headers here if needed
            requestTemplate.header("X-Source", "iam-bff-service");
        };
    }

    /**
     * Custom error decoder for Feign clients.
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

    /**
     * Custom error decoder to handle Feign client errors.
     */
    public static class FeignErrorDecoder implements ErrorDecoder {
        private final ErrorDecoder defaultErrorDecoder = new Default();

        @Override
        public Exception decode(String methodKey, feign.Response response) {
            // Log the error and return appropriate exception
            if (response.status() >= 400 && response.status() < 500) {
                return new RuntimeException("Client error: " + response.status());
            }
            if (response.status() >= 500) {
                return new RuntimeException("Server error: " + response.status());
            }
            return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}
