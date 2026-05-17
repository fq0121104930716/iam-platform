package iam.platform.common.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTenantAccountRequest {
    @Size(max = 50)
    private String employeeNo;

    @Size(max = 10)
    private String preferredLanguage;

    @Size(max = 50)
    private String timezone;
}
