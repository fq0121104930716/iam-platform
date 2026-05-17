package iam.platform.admin.application.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import iam.platform.admin.domain.model.entity.ApplicationResource;
import iam.platform.admin.domain.model.entity.ApplicationTenantMapping;
import iam.platform.admin.domain.repository.ApplicationResourceRepository;
import iam.platform.admin.domain.repository.ApplicationTenantMappingRepository;
import iam.platform.common.model.enums.ResourceType;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationManagementService {

    private final ApplicationResourceRepository applicationResourceRepository;
    private final ApplicationTenantMappingRepository applicationTenantMappingRepository;

    // ==================== Application Resource Management ====================

    @Transactional
    public ApplicationResourceResponse createResource(Long applicationId, String resourceCode,
            String resourceName, ResourceType resourceType, String icon, String path,
            String apiPath, String apiMethod, Integer sortOrder, Long parentId,
            String description) {
        ApplicationResource resource =
                ApplicationResource.create(applicationId, resourceCode, resourceName, resourceType,
                        icon, path, apiPath, apiMethod, sortOrder, parentId, description);
        resource = applicationResourceRepository.save(resource);
        log.info("Application resource created: appId={}, code={}", applicationId, resourceCode);
        return toResourceResponse(resource);
    }

    public ApplicationResourceResponse getResource(Long id) {
        ApplicationResource resource = applicationResourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found: " + id));
        return toResourceResponse(resource);
    }

    public List<ApplicationResourceResponse> getAppResources(Long applicationId) {
        return applicationResourceRepository.findByApplicationIdOrderBySortOrder(applicationId)
                .stream().map(this::toResourceResponse).toList();
    }

    public List<ApplicationResourceResponse> getAppResourcesByType(Long applicationId,
            ResourceType resourceType) {
        return applicationResourceRepository
                .findByApplicationIdAndResourceTypeOrderBySortOrder(applicationId,
                        resourceType.name())
                .stream().map(this::toResourceResponse).toList();
    }

    @Transactional
    public ApplicationResourceResponse updateResource(Long id, String resourceName, String icon,
            String path, String apiPath, String apiMethod, Integer sortOrder, String description) {
        ApplicationResource resource = applicationResourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resource not found: " + id));
        resource.update(resourceName, icon, path, apiPath, apiMethod, sortOrder, description);
        resource = applicationResourceRepository.save(resource);
        return toResourceResponse(resource);
    }

    @Transactional
    public void deleteResource(Long id) {
        applicationResourceRepository.deleteById(id);
        log.info("Application resource deleted: {}", id);
    }

    // ==================== Application-Tenant Mapping ====================

    @Transactional
    public ApplicationTenantMappingResponse assignAppToTenant(Long applicationId, Long tenantId,
            Boolean enabled) {
        if (applicationTenantMappingRepository.existsByApplicationIdAndTenantId(applicationId,
                tenantId)) {
            throw new RuntimeException("Application already assigned to tenant");
        }
        ApplicationTenantMapping mapping =
                ApplicationTenantMapping.create(applicationId, tenantId, enabled);
        mapping = applicationTenantMappingRepository.save(mapping);
        log.info("Application assigned to tenant: appId={}, tenantId={}", applicationId, tenantId);
        return toMappingResponse(mapping);
    }

    public List<ApplicationTenantMappingResponse> getTenantApps(Long tenantId) {
        return applicationTenantMappingRepository.findByTenantIdAndEnabledTrue(tenantId).stream()
                .map(this::toMappingResponse).toList();
    }

    public List<ApplicationTenantMappingResponse> getAppTenants(Long applicationId) {
        return applicationTenantMappingRepository.findByApplicationId(applicationId).stream()
                .map(this::toMappingResponse).toList();
    }

    @Transactional
    public void enableAppForTenant(Long applicationId, Long tenantId) {
        ApplicationTenantMapping mapping = applicationTenantMappingRepository
                .findByApplicationIdAndTenantId(applicationId, tenantId)
                .orElseThrow(() -> new RuntimeException("Mapping not found"));
        mapping.enable();
        applicationTenantMappingRepository.save(mapping);
    }

    @Transactional
    public void disableAppForTenant(Long applicationId, Long tenantId) {
        ApplicationTenantMapping mapping = applicationTenantMappingRepository
                .findByApplicationIdAndTenantId(applicationId, tenantId)
                .orElseThrow(() -> new RuntimeException("Mapping not found"));
        mapping.disable();
        applicationTenantMappingRepository.save(mapping);
    }

    @Transactional
    public void removeAppFromTenant(Long applicationId, Long tenantId) {
        applicationTenantMappingRepository.deleteByApplicationIdAndTenantId(applicationId,
                tenantId);
        log.info("Application removed from tenant: appId={}, tenantId={}", applicationId, tenantId);
    }

    private ApplicationResourceResponse toResourceResponse(ApplicationResource resource) {
        return ApplicationResourceResponse.builder().id(resource.getId())
                .applicationId(resource.getApplicationId()).resourceCode(resource.getResourceCode())
                .resourceName(resource.getResourceName())
                .resourceType(resource.getResourceType() != null ? resource.getResourceType().name()
                        : null)
                .icon(resource.getIcon()).path(resource.getPath()).apiPath(resource.getApiPath())
                .apiMethod(resource.getApiMethod()).sortOrder(resource.getSortOrder())
                .parentId(resource.getParentId()).description(resource.getDescription())
                .createdAt(resource.getCreatedAt()).updatedAt(resource.getUpdatedAt()).build();
    }

    private ApplicationTenantMappingResponse toMappingResponse(ApplicationTenantMapping mapping) {
        return ApplicationTenantMappingResponse.builder().id(mapping.getId())
                .applicationId(mapping.getApplicationId()).tenantId(mapping.getTenantId())
                .enabled(mapping.isEnabled()).createdAt(mapping.getCreatedAt()).build();
    }

    @Builder
    public static class ApplicationResourceResponse {
        public Long id, applicationId;
        public String resourceCode, resourceName, resourceType, icon, path, apiPath, apiMethod,
                description;
        public Integer sortOrder;
        public Long parentId;
        public java.time.LocalDateTime createdAt, updatedAt;
    }

    @Builder
    public static class ApplicationTenantMappingResponse {
        public Long id, applicationId, tenantId;
        public Boolean enabled;
        public java.time.LocalDateTime createdAt;
    }
}
