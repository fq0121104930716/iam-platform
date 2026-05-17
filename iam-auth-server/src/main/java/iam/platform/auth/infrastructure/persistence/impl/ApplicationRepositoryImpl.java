package iam.platform.auth.infrastructure.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import iam.platform.auth.domain.model.entity.Application;
import iam.platform.common.model.enums.AppStatus;
import iam.platform.common.model.enums.AppType;
import iam.platform.auth.domain.repository.ApplicationRepository;
import iam.platform.auth.infrastructure.persistence.entity.ApplicationPO;
import iam.platform.auth.infrastructure.persistence.repository.ApplicationJpaRepository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ApplicationRepositoryImpl implements ApplicationRepository {

    private final ApplicationJpaRepository jpaRepository;

    @Override
    public Application save(Application app) {
        ApplicationPO po = toPO(app);
        po = jpaRepository.save(po);
        return toDomain(po);
    }

    @Override
    public Optional<Application> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Application> findByAppId(String appId) {
        return jpaRepository.findByAppId(appId).map(this::toDomain);
    }

    @Override
    public List<Application> findByTenantId(Long tenantId) {
        return jpaRepository.findByTenantId(tenantId).stream().map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Application> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long countByTenantIdAndStatus(Long tenantId, String status) {
        return jpaRepository.countByTenantIdAndStatus(tenantId, status);
    }

    @Override
    public long countByStatus(String status) {
        return jpaRepository.countByStatus(status);
    }

    private ApplicationPO toPO(Application app) {
        ApplicationPO po = new ApplicationPO();
        po.setId(app.getId());
        po.setAppId(app.getAppId());
        po.setAppSecret(app.getAppSecret());
        po.setAppName(app.getAppName());
        po.setTenantId(app.getTenantId());
        po.setAppType(app.getAppType() != null ? app.getAppType().name() : null);
        po.setDescription(app.getDescription());
        po.setLogoUrl(app.getLogoUrl());
        po.setStatus(app.getStatus() != null ? app.getStatus().name() : null);
        po.setHomePageUrl(app.getHomePageUrl());
        po.setCallbackUrls(joinSet(app.getCallbackUrls()));
        po.setPostLogoutRedirectUris(joinSet(app.getPostLogoutRedirectUris()));
        po.setAllowedScopes(joinSet(app.getAllowedScopes()));
        po.setRequireProofKey(app.isRequireProofKey());
        po.setRequireAuthorizationConsent(app.isRequireAuthorizationConsent());
        po.setAccessTokenTtlSeconds(app.getAccessTokenTtlSeconds());
        po.setRefreshTokenTtlSeconds(app.getRefreshTokenTtlSeconds());
        po.setEnabled(app.isEnabled());
        po.setCreatedAt(app.getCreatedAt());
        po.setUpdatedAt(app.getUpdatedAt());
        return po;
    }

    private Application toDomain(ApplicationPO po) {
        return Application.builder().id(po.getId()).appId(po.getAppId())
                .appSecret(po.getAppSecret()).appName(po.getAppName()).tenantId(po.getTenantId())
                .appType(parseAppType(po.getAppType())).description(po.getDescription())
                .logoUrl(po.getLogoUrl()).status(parseAppStatus(po.getStatus()))
                .homePageUrl(po.getHomePageUrl()).callbackUrls(splitSet(po.getCallbackUrls()))
                .postLogoutRedirectUris(splitSet(po.getPostLogoutRedirectUris()))
                .allowedScopes(splitSet(po.getAllowedScopes()))
                .requireProofKey(po.isRequireProofKey())
                .requireAuthorizationConsent(po.isRequireAuthorizationConsent())
                .accessTokenTtlSeconds(po.getAccessTokenTtlSeconds())
                .refreshTokenTtlSeconds(po.getRefreshTokenTtlSeconds()).enabled(po.isEnabled())
                .createdAt(po.getCreatedAt()).updatedAt(po.getUpdatedAt()).build();
    }

    private AppType parseAppType(String value) {
        if (value == null || value.isBlank()) {
            return AppType.WEB;
        }
        try {
            return AppType.valueOf(value);
        } catch (IllegalArgumentException e) {
            return AppType.WEB;
        }
    }

    private AppStatus parseAppStatus(String value) {
        if (value == null || value.isBlank()) {
            return AppStatus.ACTIVE;
        }
        try {
            return AppStatus.valueOf(value);
        } catch (IllegalArgumentException e) {
            return AppStatus.ACTIVE;
        }
    }

    private String joinSet(Set<String> set) {
        if (set == null || set.isEmpty()) {
            return "";
        }
        return String.join(",", set);
    }

    private Set<String> splitSet(String str) {
        if (str == null || str.isEmpty()) {
            return new HashSet<>();
        }
        return Arrays.stream(str.split(",")).collect(Collectors.toSet());
    }
}
