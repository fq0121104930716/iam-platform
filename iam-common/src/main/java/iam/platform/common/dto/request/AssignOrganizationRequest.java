package iam.platform.common.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignOrganizationRequest {
    @NotNull(message = "Organization ID is required")
    private Long organizationId;

    private Boolean isPrimary;

    private String position;
}
