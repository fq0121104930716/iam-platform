package iam.platform.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCredentialResponse {
    private Long id;
    private Long userId;
    private String credentialType;
    private String algorithm;
    private boolean isPrimary;
    private LocalDateTime expiresAt;
    private LocalDateTime lastUsedAt;
    private String status;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
