package iam.platform.common.dto.request;

import iam.platform.common.validation.ValidCredentialType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserCredentialRequest {
    @NotBlank(message = "Credential type is required")
    @ValidCredentialType(message = "Invalid credential type. Must be one of: PASSWORD, CERTIFICATE")
    private String credentialType;

    @NotBlank(message = "Credential value is required")
    private String credentialValue;

    private String algorithm;

    @Builder.Default
    private Boolean isPrimary = false;

    private LocalDateTime expiresAt;

    private String description;
}
