package iam.platform.common.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTenantRequest {
    @NotBlank(message = "Tenant code is required")
    @Pattern(regexp = "^[a-z0-9-]+$",
            message = "Tenant code must be lowercase letters, numbers, and hyphens only")
    @Size(min = 3, max = 50, message = "Tenant code must be between 3 and 50 characters")
    private String tenantCode;

    @NotBlank(message = "Tenant name is required")
    @Size(min = 2, max = 200, message = "Tenant name must be between 2 and 200 characters")
    private String tenantName;

    @Min(value = 10, message = "Max users must be at least 10")
    private Integer maxUsers;

    @Email(message = "Contact email format is invalid")
    private String contactEmail;

    @Size(max = 20)
    private String contactPhone;

    @Future(message = "Expires at must be in the future")
    private LocalDateTime expiresAt;
}
