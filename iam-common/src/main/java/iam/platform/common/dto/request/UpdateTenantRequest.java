package iam.platform.common.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
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
public class UpdateTenantRequest {
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
