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
public class TenantAccountRoleMapping {
    private Long id;
    private Long tenantAccountId;
    private Long roleId;
    private LocalDateTime assignedAt;
    private String assignedBy;
}
