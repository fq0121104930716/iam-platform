package iam.platform.bff.infrastructure.filter;

import iam.platform.common.context.GatewayHeaderContextFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Configuration for BFF gateway header context filter.
 *
 * <p>Registers the {@link GatewayHeaderContextFilter} to extract tenant context
 * from gateway headers for all incoming requests to the BFF service.
 */
@Configuration
public class BffGatewayHeaderFilter {

    @Bean
    public FilterRegistrationBean<GatewayHeaderContextFilter> gatewayHeaderContextFilter() {
        FilterRegistrationBean<GatewayHeaderContextFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new GatewayHeaderContextFilter());
        registration.addUrlPatterns("/*");
        registration.setName("gatewayHeaderContextFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return registration;
    }
}
