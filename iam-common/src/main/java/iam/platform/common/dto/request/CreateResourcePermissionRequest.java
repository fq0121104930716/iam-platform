package iam.platform.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import iam.platform.common.model.enums.PermissionAction;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateResourcePermissionRequest {
    @NotBlank(message = "Permission code is required")
    private String permissionCode;

    @NotBlank(message = "Permission name is required")
    private String permissionName;

    @NotBlank(message = "Resource type is required")
    private String resourceType;

    @NotNull(message = "Action is required")
    private PermissionAction action;

    private String description;
}
