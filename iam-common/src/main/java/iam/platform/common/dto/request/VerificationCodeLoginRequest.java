package iam.platform.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationCodeLoginRequest {

    @NotBlank(message = "type is required, must be 'sms' or 'email'")
    private String type;

    @NotBlank(message = "identifier is required (phone number or email)")
    private String identifier;

    @NotBlank(message = "verification code is required")
    private String code;
}
