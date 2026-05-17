package iam.platform.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import iam.platform.common.model.enums.OrgType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrganizationRequest {
    @NotBlank(message = "Organization code is required")
    @Size(min = 2, max = 50, message = "Organization code must be between 2 and 50 characters")
    private String orgCode;

    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 200, message = "Organization name must be between 2 and 200 characters")
    private String orgName;

    @NotNull(message = "Organization type is required")
    private OrgType orgType;

    private Long parentId;

    private Long managerId;

    private Integer sortOrder;

    @Size(max = 20)
    private String phone;

    @Size(max = 255)
    private String email;

    @Size(max = 500)
    private String description;
}
