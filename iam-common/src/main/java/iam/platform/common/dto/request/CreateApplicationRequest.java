package iam.platform.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateApplicationRequest {
    @NotBlank(message = "Application name is required")
    private String appName;

    @NotNull(message = "Tenant ID is required")
    private Long tenantId;

    @NotNull(message = "Application type is required")
    private String appType; // WEB, MOBILE, API, THIRD_PARTY

    private String description;
    private String logoUrl;
    private String homePageUrl;

    @NotEmpty(message = "At least one callback URI is required")
    private List<String> callbackUrls;

    private List<String> postLogoutRedirectUris;

    @NotEmpty(message = "At least one scope is required")
    private List<String> allowedScopes;

    private boolean requirePkce;
    private boolean requireAuthorizationConsent;
    private Integer accessTokenTtlSeconds;
    private Integer refreshTokenTtlSeconds;
}
