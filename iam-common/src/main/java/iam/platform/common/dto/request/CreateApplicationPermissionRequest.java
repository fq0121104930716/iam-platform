package iam.platform.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateApplicationPermissionRequest {
    @NotBlank(message = "Permission code is required")
    private String permissionCode;

    @NotBlank(message = "Permission name is required")
    private String permissionName;

    @NotBlank(message = "Resource type is required")
    private String resourceType;

    @NotBlank(message = "Action is required")
    private String action; // READ, WRITE, DELETE, EXECUTE

    private String description;
}
