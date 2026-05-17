package iam.platform.audit.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * SIEM endpoint domain entity.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiemEndpoint {

    private Long id;
    private String endpointName;
    private String endpointType;
    private String endpointUrl;
    private String authConfig;
    private String format;
    private Boolean enabled;
    private Integer batchSize;
    private Integer batchIntervalSeconds;
    private LocalDateTime lastExportAt;

    public static SiemEndpoint create(String endpointName, String endpointType, String endpointUrl,
                                       String authConfig, String format, Integer batchSize,
                                       Integer batchIntervalSeconds) {
        return SiemEndpoint.builder()
                .endpointName(endpointName)
                .endpointType(endpointType)
                .endpointUrl(endpointUrl)
                .authConfig(authConfig)
                .format(format)
                .enabled(false)
                .batchSize(batchSize != null ? batchSize : 100)
                .batchIntervalSeconds(batchIntervalSeconds != null ? batchIntervalSeconds : 30)
                .build();
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public void recordExport() {
        this.lastExportAt = LocalDateTime.now();
    }
}
