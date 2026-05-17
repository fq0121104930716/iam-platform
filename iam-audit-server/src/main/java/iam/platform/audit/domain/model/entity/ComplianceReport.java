package iam.platform.audit.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Compliance report domain entity.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceReport {

    private Long id;
    private String reportCode;
    private String reportType;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private LocalDateTime generatedAt;
    private String generatedBy;
    private String status;
    private String filePath;
    private String summaryJson;

    public static ComplianceReport create(String reportCode, String reportType,
                                           LocalDateTime periodStart, LocalDateTime periodEnd,
                                           String generatedBy) {
        return ComplianceReport.builder()
                .reportCode(reportCode)
                .reportType(reportType)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .generatedBy(generatedBy)
                .status("GENERATING")
                .build();
    }

    public void markCompleted(String filePath, String summaryJson) {
        this.status = "COMPLETED";
        this.filePath = filePath;
        this.summaryJson = summaryJson;
        this.generatedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = "FAILED";
        this.generatedAt = LocalDateTime.now();
    }
}
