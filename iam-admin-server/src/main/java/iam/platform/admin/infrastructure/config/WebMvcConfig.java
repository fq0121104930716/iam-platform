package iam.platform.admin.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration.
 *
 * <p>Tenant context is now handled by {@link iam.platform.admin.infrastructure.security.JwtUserContextFilter}
 * which reads standardized headers from the gateway. The old TenantInterceptor has been removed.
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
}
