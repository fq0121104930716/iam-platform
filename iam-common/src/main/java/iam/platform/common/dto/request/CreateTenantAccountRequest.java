package iam.platform.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTenantAccountRequest {
    @NotNull(message = "Tenant ID is required")
    private Long tenantId;

    @NotBlank(message = "Account code is required")
    @Size(min = 3, max = 50, message = "Account code must be between 3 and 50 characters")
    private String accountCode;

    @Size(max = 50)
    private String employeeNo;

    @Size(max = 10)
    private String preferredLanguage;

    @Size(max = 50)
    private String timezone;
}
