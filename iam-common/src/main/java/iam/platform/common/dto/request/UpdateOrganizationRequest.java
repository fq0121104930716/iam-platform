package iam.platform.common.dto.request;

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
public class UpdateOrganizationRequest {
    @Size(min = 2, max = 200, message = "Organization name must be between 2 and 200 characters")
    private String orgName;

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
