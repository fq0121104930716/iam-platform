package iam.platform.bff.infrastructure.config;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import iam.platform.common.context.TenantContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Feign clients.
 *
 * <p>Intercepts Feign requests to add tenant context headers for downstream service calls.
 */
@Configuration
public class FeignClientConfig {

    /**
     * Add tenant context headers to Feign requests.
     *
     * <p>Headers are read from the current {@link TenantContext}, which was populated
     * by the gateway via standardized HTTP headers.
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // Add source identifier
            requestTemplate.header("X-Source", "iam-bff-service");

            // Add tenant context headers from current thread local
            Long userId = TenantContext.getCurrentUserId();
            if (userId != null) {
                requestTemplate.header(TenantContext.HEADER_USER_ID, userId.toString());
            }

            String userName = TenantContext.getCurrentUserName();
            if (userName != null) {
                requestTemplate.header(TenantContext.HEADER_USER_NAME, userName);
            }

            Long tenantId = TenantContext.getCurrentTenantId();
            if (tenantId != null) {
                requestTemplate.header(TenantContext.HEADER_TENANT_ID, tenantId.toString());
            }

            Long tenantAccountId = TenantContext.getCurrentTenantAccountId();
            if (tenantAccountId != null) {
                requestTemplate.header(TenantContext.HEADER_TENANT_ACCOUNT_ID, tenantAccountId.toString());
            }
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
