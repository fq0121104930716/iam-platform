package iam.platform.admin.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantAccountOrganizationMapping {
    private Long id;
    private Long tenantAccountId;
    private Long organizationId;
    private Boolean isPrimary;
    private String position;
    private LocalDateTime joinedOrgAt;
}
