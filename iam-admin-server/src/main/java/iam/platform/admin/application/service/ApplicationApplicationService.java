package iam.platform.admin.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import iam.platform.common.dto.request.CreateApplicationPermissionRequest;
import iam.platform.common.dto.request.CreateApplicationRequest;
import iam.platform.common.dto.request.UpdateApplicationRequest;
import iam.platform.common.dto.response.ApplicationCreatedResponse;
import iam.platform.common.dto.response.ApplicationPermissionResponse;
import iam.platform.common.dto.response.ApplicationResponse;
import iam.platform.common.model.annotation.AuditLog;
import iam.platform.admin.domain.model.entity.Application;
import iam.platform.admin.domain.model.entity.ApplicationPermission;
import iam.platform.common.model.enums.AppType;
import iam.platform.common.model.enums.AuditEventType;
import iam.platform.common.model.enums.PermissionAction;
import iam.platform.common.model.valueobject.TokenSettings;
import iam.platform.admin.domain.repository.ApplicationPermissionRepository;
import iam.platform.admin.domain.repository.ApplicationRepository;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationPermissionRepository permissionRepository;

    @Transactional
    @AuditLog(value = AuditEventType.APPLICATION_CREATED, resourceType = "application", action = "创建应用 #{#request.appName}")
    public ApplicationCreatedResponse createApplication(CreateApplicationRequest request) {
        // Domain factory handles credential generation, defaults, and state
        TokenSettings tokenSettings = TokenSettings.of(
                request.getAccessTokenTtlSeconds(),
                request.getRefreshTokenTtlSeconds());

        Application app = Application.register(
                request.getAppName(),
                request.getTenantId(),
                AppType.valueOf(request.getAppType()),
                request.getDescription(),
                request.getLogoUrl(),
                request.getHomePageUrl(),
                request.getCallbackUrls() != null ? new HashSet<>(request.getCallbackUrls()) : null,
                request.getPostLogoutRedirectUris() != null ? new HashSet<>(request.getPostLogoutRedirectUris()) : null,
                request.getAllowedScopes() != null ? new HashSet<>(request.getAllowedScopes()) : null,
                request.isRequirePkce(),
                request.isRequireAuthorizationConsent(),
                tokenSettings);

        app = applicationRepository.save(app);
        log.info("Application created: {} ({}) for tenant {}", app.getAppName(), app.getAppId(),
                app.getTenantId());

        return toCreatedResponse(app);
    }

    public ApplicationResponse getApplication(Long id) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found: " + id));
        return toResponse(app);
    }

    public ApplicationResponse getApplicationByAppId(String appId) {
        Application app = applicationRepository.findByAppId(appId)
                .orElseThrow(() -> new RuntimeException("Application not found: " + appId));
        return toResponse(app);
    }

    public List<ApplicationResponse> getApplicationsByTenantId(Long tenantId) {
        return applicationRepository.findByTenantId(tenantId).stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ApplicationResponse> listAllApplications() {
        return applicationRepository.findAll().stream().map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @AuditLog(value = AuditEventType.APPLICATION_UPDATED, resourceType = "application", action = "更新应用 ID=#{#id}")
    public ApplicationResponse updateApplication(Long id, UpdateApplicationRequest request) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found: " + id));

        // Delegate to domain behavior methods
        app.updateMetadata(
                request.getAppName(),
                request.getDescription(),
                request.getLogoUrl(),
                request.getHomePageUrl());

        if (request.getCallbackUrls() != null || request.getPostLogoutRedirectUris() != null
                || request.getAllowedScopes() != null || request.getRequirePkce() != null
                || request.getRequireAuthorizationConsent() != null) {
            app.updateOAuthSettings(
                    request.getCallbackUrls() != null ? new HashSet<>(request.getCallbackUrls()) : null,
                    request.getPostLogoutRedirectUris() != null ? new HashSet<>(request.getPostLogoutRedirectUris())
                            : null,
                    request.getAllowedScopes() != null ? new HashSet<>(request.getAllowedScopes()) : null,
                    request.getRequirePkce() != null ? request.getRequirePkce() : app.isRequireProofKey(),
                    request.getRequireAuthorizationConsent() != null ? request.getRequireAuthorizationConsent()
                            : app.isRequireAuthorizationConsent());
        }

        if (request.getAccessTokenTtlSeconds() != null || request.getRefreshTokenTtlSeconds() != null) {
            TokenSettings newSettings = TokenSettings.of(
                    request.getAccessTokenTtlSeconds() != null ? request.getAccessTokenTtlSeconds()
                            : app.getAccessTokenTtlSeconds(),
                    request.getRefreshTokenTtlSeconds() != null ? request.getRefreshTokenTtlSeconds()
                            : app.getRefreshTokenTtlSeconds());
            app.updateTokenSettings(newSettings);
        }

        app = applicationRepository.save(app);
        log.info("Application updated: {}", app.getAppId());
        return toResponse(app);
    }

    @Transactional
    @AuditLog(value = AuditEventType.APPLICATION_DELETED, resourceType = "application", action = "删除应用 ID=#{#id}")
    public void deleteApplication(Long id) {
        if (!applicationRepository.findById(id).isPresent()) {
            throw new RuntimeException("Application not found: " + id);
        }
        applicationRepository.deleteById(id);
        log.info("Application deleted: {}", id);
    }

    @Transactional
    public ApplicationCreatedResponse rotateAppSecret(Long id) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found: " + id));

        // Domain entity handles secret rotation
        app.rotateSecret();
        app = applicationRepository.save(app);
        log.info("Application secret rotated for: {}", app.getAppId());

        return toCreatedResponse(app);
    }

    @Transactional
    @AuditLog(value = AuditEventType.APPLICATION_ACTIVATED, resourceType = "application", action = "激活应用 ID=#{#id}")
    public void activateApplication(Long id) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found: " + id));
        app.activate();
        applicationRepository.save(app);
        log.info("Application {} activated", app.getAppId());
    }

    @Transactional
    public void deactivateApplication(Long id) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found: " + id));
        app.deactivate();
        applicationRepository.save(app);
        log.info("Application {} deactivated", app.getAppId());
    }

    @Transactional
    @AuditLog(value = AuditEventType.APPLICATION_BLOCKED, resourceType = "application", action = "封禁应用 ID=#{#id}")
    public void blockApplication(Long id) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found: " + id));
        app.block();
        applicationRepository.save(app);
        log.info("Application {} blocked", app.getAppId());
    }

    // === Application Permission Management ===

    @Transactional
    public ApplicationPermissionResponse createPermission(Long applicationId,
            CreateApplicationPermissionRequest request) {
        if (!applicationRepository.findById(applicationId).isPresent()) {
            throw new RuntimeException("Application not found: " + applicationId);
        }

        PermissionAction action = PermissionAction.valueOf(request.getAction().toUpperCase());
        ApplicationPermission permission = ApplicationPermission.create(applicationId,
                request.getResourceType(), action, request.getPermissionName(),
                request.getDescription());

        permission = permissionRepository.save(permission);
        log.info("Permission created: {} for application {}", permission.getPermissionCode(),
                applicationId);
        return toPermissionResponse(permission);
    }

    public List<ApplicationPermissionResponse> getPermissionsByApplicationId(Long applicationId) {
        return permissionRepository.findByApplicationId(applicationId).stream()
                .map(this::toPermissionResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deletePermission(Long permissionId) {
        if (!permissionRepository.findById(permissionId).isPresent()) {
            throw new RuntimeException("Permission not found: " + permissionId);
        }
        permissionRepository.deleteById(permissionId);
        log.info("Permission deleted: {}", permissionId);
    }

    // === DTO Converters ===

    private ApplicationCreatedResponse toCreatedResponse(Application app) {
        return ApplicationCreatedResponse.builder().id(app.getId()).appId(app.getAppId())
                .appSecret(app.getAppSecret()).appName(app.getAppName()).tenantId(app.getTenantId())
                .appType(app.getAppType() != null ? app.getAppType().name() : null)
                .description(app.getDescription()).logoUrl(app.getLogoUrl())
                .status(app.getStatus() != null ? app.getStatus().name() : null)
                .homePageUrl(app.getHomePageUrl()).callbackUrls(List.copyOf(app.getCallbackUrls()))
                .postLogoutRedirectUris(List.copyOf(app.getPostLogoutRedirectUris()))
                .allowedScopes(List.copyOf(app.getAllowedScopes()))
                .requirePkce(app.isRequireProofKey())
                .requireAuthorizationConsent(app.isRequireAuthorizationConsent())
                .enabled(app.isEnabled()).createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt()).build();
    }

    private ApplicationResponse toResponse(Application app) {
        return ApplicationResponse.builder().id(app.getId()).appId(app.getAppId())
                .appName(app.getAppName()).tenantId(app.getTenantId())
                .appType(app.getAppType() != null ? app.getAppType().name() : null)
                .description(app.getDescription()).logoUrl(app.getLogoUrl())
                .status(app.getStatus() != null ? app.getStatus().name() : null)
                .homePageUrl(app.getHomePageUrl()).callbackUrls(List.copyOf(app.getCallbackUrls()))
                .postLogoutRedirectUris(List.copyOf(app.getPostLogoutRedirectUris()))
                .allowedScopes(List.copyOf(app.getAllowedScopes()))
                .requirePkce(app.isRequireProofKey())
                .requireAuthorizationConsent(app.isRequireAuthorizationConsent())
                .enabled(app.isEnabled()).createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt()).build();
    }

    private ApplicationPermissionResponse toPermissionResponse(ApplicationPermission permission) {
        return ApplicationPermissionResponse.builder().id(permission.getId())
                .applicationId(permission.getApplicationId())
                .permissionCode(permission.getPermissionCode())
                .permissionName(permission.getPermissionName())
                .resourceType(permission.getResourceType())
                .action(permission.getAction() != null ? permission.getAction().name() : null)
                .description(permission.getDescription()).createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt()).build();
    }
}
