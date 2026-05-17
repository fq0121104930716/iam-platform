package iam.platform.admin.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the audit log system.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "sso.audit")
public class AuditProperties {

    /**
     * Whether audit logging is enabled.
     */
    private boolean enabled = true;

    /**
     * Number of days to retain audit logs (default 180 days).
     */
    private int retentionDays = 180;

    /**
     * Async thread pool configuration for audit log writing.
     */
    private AsyncProperties async = new AsyncProperties();

    @Data
    public static class AsyncProperties {
        private int corePoolSize = 2;
        private int maxPoolSize = 5;
        private int queueCapacity = 500;
    }
}
