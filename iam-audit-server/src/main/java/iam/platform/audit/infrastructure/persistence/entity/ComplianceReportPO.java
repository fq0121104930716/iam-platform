package iam.platform.audit.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Compliance report persistence entity (JPA).
 */
@Entity
@Table(name = "t_compliance_report", indexes = {
    @Index(name = "idx_compliance_type", columnList = "report_type"),
    @Index(name = "idx_compliance_status", columnList = "status"),
    @Index(name = "idx_compliance_period", columnList = "period_start, period_end")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceReportPO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_code", nullable = false, unique = true, length = 50)
    private String reportCode;

    @Column(name = "report_type", nullable = false, length = 30)
    private String reportType;

    @Column(name = "period_start")
    private LocalDateTime periodStart;

    @Column(name = "period_end")
    private LocalDateTime periodEnd;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "generated_by", length = 100)
    private String generatedBy;

    @Column(length = 20)
    private String status;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "summary_json", columnDefinition = "TEXT")
    private String summaryJson;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = "GENERATING";
        }
    }
}
