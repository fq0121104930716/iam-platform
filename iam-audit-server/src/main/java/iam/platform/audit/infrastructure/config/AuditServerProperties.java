package iam.platform.audit.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration properties for audit server.
 */
@Data
@ConfigurationProperties(prefix = "audit")
public class AuditServerProperties {

    /** Encryption configuration */
    private EncryptionConfig encryption = new EncryptionConfig();

    /** Alert engine configuration */
    private AlertConfig alert = new AlertConfig();

    /** SIEM export configuration */
    private SiemConfig siem = new SiemConfig();

    /** Compliance report configuration */
    private ComplianceConfig compliance = new ComplianceConfig();

    @Data
    public static class EncryptionConfig {
        /** AES encryption key (32 chars for AES-256) */
        private String key;

        /** Fields to encrypt by default */
        private List<String> sensitiveFields = List.of("requestParams", "username");
    }

    @Data
    public static class AlertConfig {
        /** Alert evaluation interval in milliseconds */
        private Long evaluationIntervalMs = 1000L;

        /** Notification configuration */
        private NotificationConfig notification = new NotificationConfig();
    }

    @Data
    public static class NotificationConfig {
        private EmailConfig email = new EmailConfig();
        private WebhookConfig webhook = new WebhookConfig();
    }

    @Data
    public static class EmailConfig {
        private Boolean enabled = false;
        private String to = "admin@example.com";
    }

    @Data
    public static class WebhookConfig {
        private Boolean enabled = false;
        private String url;
    }

    @Data
    public static class SiemConfig {
        /** Export interval in seconds */
        private Integer exportIntervalSeconds = 30;

        /** Batch size for export */
        private Integer batchSize = 100;
    }

    @Data
    public static class ComplianceConfig {
        /** Report storage path */
        private String reportStoragePath = "/tmp/audit-reports";
    }
}
