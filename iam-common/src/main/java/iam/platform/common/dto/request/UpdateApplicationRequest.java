package iam.platform.common.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateApplicationRequest {
    private String appName;
    private String description;
    private String logoUrl;
    private String homePageUrl;
    private String status; // ACTIVE, INACTIVE, REVIEWING, BLOCKED

    private List<String> callbackUrls;
    private List<String> postLogoutRedirectUris;

    @NotEmpty(message = "At least one scope is required")
    private List<String> allowedScopes;

    private Boolean requirePkce;
    private Boolean requireAuthorizationConsent;
    private Integer accessTokenTtlSeconds;
    private Integer refreshTokenTtlSeconds;
    private Boolean enabled;
}
