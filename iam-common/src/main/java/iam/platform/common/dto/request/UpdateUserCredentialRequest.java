package iam.platform.common.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserCredentialRequest {
    private String credentialValue;

    private LocalDateTime expiresAt;

    private String description;

    private Boolean isPrimary;
}
