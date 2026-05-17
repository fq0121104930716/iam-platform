package iam.platform.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {
    private Long id;
    private String appId;
    private String appName;
    private Long tenantId;
    private String appType;
    private String description;
    private String logoUrl;
    private String status;
    private String homePageUrl;
    private List<String> callbackUrls;
    private List<String> postLogoutRedirectUris;
    private List<String> allowedScopes;
    private boolean requirePkce;
    private boolean requireAuthorizationConsent;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
