package iam.platform.auth.infrastructure.config;

import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Feign client configuration with timeout settings.
 */
@Configuration
public class FeignClientConfig {

    @Bean
    public Request.Options options() {
        return new Request.Options(1000, TimeUnit.MILLISECONDS, 3000, TimeUnit.MILLISECONDS, true); // 连接超时1秒，读取超时3秒
    }
}
