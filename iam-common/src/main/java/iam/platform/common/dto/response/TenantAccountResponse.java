package iam.platform.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantAccountResponse {
    private Long id;
    private Long personId;
    private Long tenantId;
    private String tenantCode;
    private String tenantName;
    private String accountCode;
    private String employeeNo;
    private String status;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;
    private String preferredLanguage;
    private String timezone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
