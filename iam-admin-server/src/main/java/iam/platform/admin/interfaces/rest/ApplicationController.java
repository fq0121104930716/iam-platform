package iam.platform.admin.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import iam.platform.admin.application.service.ApplicationManagementService;
import iam.platform.admin.application.service.ApplicationManagementService.ApplicationResourceResponse;
import iam.platform.admin.application.service.ApplicationManagementService.ApplicationTenantMappingResponse;
import iam.platform.common.model.enums.ResourceType;
import iam.platform.common.api.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/v1/applications")
@RequiredArgsConstructor
@Tag(name = "Application Management", description = "Application resource and tenant assignment API")
public class ApplicationController {

    private final ApplicationManagementService applicationManagementService;

    // ==================== Application Resource Management ====================

    @PostMapping("/{appId}/resources")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create application resource")
    public ApiResponse<ApplicationResourceResponse> createResource(
            @PathVariable Long appId, @RequestParam String resourceCode,
            @RequestParam String resourceName, @RequestParam ResourceType resourceType,
            @RequestParam(required = false) String icon,
            @RequestParam(required = false) String path,
            @RequestParam(required = false) String apiPath,
            @RequestParam(required = false) String apiMethod,
            @RequestParam(defaultValue = "0") Integer sortOrder,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) String description) {
        return ApiResponse.created(
                applicationManagementService.createResource(appId, resourceCode, resourceName,
                        resourceType, icon, path, apiPath, apiMethod, sortOrder, parentId, description));
    }

    @GetMapping("/resources/{id}")
    @Operation(summary = "Get application resource by ID")
    public ApiResponse<ApplicationResourceResponse> getResource(@PathVariable Long id) {
        return ApiResponse.success(applicationManagementService.getResource(id));
    }

    @GetMapping("/{appId}/resources")
    @Operation(summary = "List application resources")
    public ApiResponse<List<ApplicationResourceResponse>> getAppResources(
            @PathVariable Long appId) {
        return ApiResponse.success(applicationManagementService.getAppResources(appId));
    }

    @GetMapping("/{appId}/resources/type/{resourceType}")
    @Operation(summary = "List application resources by type")
    public ApiResponse<List<ApplicationResourceResponse>> getAppResourcesByType(
            @PathVariable Long appId, @PathVariable ResourceType resourceType) {
        return ApiResponse.success(
                applicationManagementService.getAppResourcesByType(appId, resourceType));
    }

    @PutMapping("/resources/{id}")
    @Operation(summary = "Update application resource")
    public ApiResponse<ApplicationResourceResponse> updateResource(@PathVariable Long id,
            @RequestParam(required = false) String resourceName,
            @RequestParam(required = false) String icon,
            @RequestParam(required = false) String path,
            @RequestParam(required = false) String apiPath,
            @RequestParam(required = false) String apiMethod,
            @RequestParam(required = false) Integer sortOrder,
            @RequestParam(required = false) String description) {
        return ApiResponse.success(
                applicationManagementService.updateResource(id, resourceName, icon, path,
                        apiPath, apiMethod, sortOrder, description));
    }

    @DeleteMapping("/resources/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete application resource")
    public void deleteResource(@PathVariable Long id) {
        applicationManagementService.deleteResource(id);
    }

    // ==================== Application-Tenant Mapping ====================

    @PostMapping("/{appId}/tenants/{tenantId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Assign application to tenant")
    public ApiResponse<ApplicationTenantMappingResponse> assignAppToTenant(
            @PathVariable Long appId, @PathVariable Long tenantId,
            @RequestParam(defaultValue = "true") Boolean enabled) {
        return ApiResponse.created(
                applicationManagementService.assignAppToTenant(appId, tenantId, enabled));
    }

    @GetMapping("/tenants/{tenantId}")
    @Operation(summary = "Get tenant's applications")
    public ApiResponse<List<ApplicationTenantMappingResponse>> getTenantApps(
            @PathVariable Long tenantId) {
        return ApiResponse.success(applicationManagementService.getTenantApps(tenantId));
    }

    @GetMapping("/{appId}/tenants")
    @Operation(summary = "Get application's tenant assignments")
    public ApiResponse<List<ApplicationTenantMappingResponse>> getAppTenants(
            @PathVariable Long appId) {
        return ApiResponse.success(applicationManagementService.getAppTenants(appId));
    }

    @PutMapping("/{appId}/tenants/{tenantId}/enable")
    @Operation(summary = "Enable application for tenant")
    public ApiResponse<Void> enableAppForTenant(@PathVariable Long appId,
            @PathVariable Long tenantId) {
        applicationManagementService.enableAppForTenant(appId, tenantId);
        return ApiResponse.success(null);
    }

    @PutMapping("/{appId}/tenants/{tenantId}/disable")
    @Operation(summary = "Disable application for tenant")
    public ApiResponse<Void> disableAppForTenant(@PathVariable Long appId,
            @PathVariable Long tenantId) {
        applicationManagementService.disableAppForTenant(appId, tenantId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{appId}/tenants/{tenantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove application from tenant")
    public void removeAppFromTenant(@PathVariable Long appId, @PathVariable Long tenantId) {
        applicationManagementService.removeAppFromTenant(appId, tenantId);
    }
}
