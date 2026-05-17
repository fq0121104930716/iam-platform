package iam.platform.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import iam.platform.common.model.enums.PermissionAction;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {
    private Long id;
    private Long tenantId;
    private String permissionCode;
    private String permissionName;
    private String resourceType;
    private PermissionAction action;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
