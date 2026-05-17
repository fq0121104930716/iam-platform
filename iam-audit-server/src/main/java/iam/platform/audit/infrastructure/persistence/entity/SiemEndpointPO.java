package iam.platform.audit.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SIEM endpoint persistence entity (JPA).
 */
@Entity
@Table(name = "t_siem_endpoint", indexes = {
    @Index(name = "idx_siem_enabled", columnList = "enabled")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiemEndpointPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "endpoint_name", nullable = false, length = 100)
    private String endpointName;

    @Column(name = "endpoint_type", nullable = false, length = 20)
    private String endpointType;

    @Column(name = "endpoint_url", length = 500)
    private String endpointUrl;

    @Column(name = "auth_config", columnDefinition = "TEXT")
    private String authConfig;

    @Column(length = 20)
    private String format;

    private Boolean enabled;

    @Column(name = "batch_size")
    private Integer batchSize;

    @Column(name = "batch_interval_seconds")
    private Integer batchIntervalSeconds;

    @Column(name = "last_export_at")
    private LocalDateTime lastExportAt;

    @PrePersist
    public void prePersist() {
        if (enabled == null) {
            enabled = false;
        }
        if (batchSize == null) {
            batchSize = 100;
        }
        if (batchIntervalSeconds == null) {
            batchIntervalSeconds = 30;
        }
        if (format == null) {
            format = "JSON";
        }
    }
}
