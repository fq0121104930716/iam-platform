package iam.platform.admin.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import iam.platform.common.dto.request.CreateApplicationPermissionRequest;
import iam.platform.common.dto.request.CreateApplicationRequest;
import iam.platform.common.dto.request.UpdateApplicationRequest;
import iam.platform.common.dto.response.ApplicationCreatedResponse;
import iam.platform.common.dto.response.ApplicationPermissionResponse;
import iam.platform.common.dto.response.ApplicationResponse;
import iam.platform.admin.application.service.ApplicationApplicationService;
import iam.platform.common.api.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Tag(name = "Application Management", description = "Application and permission management API")
public class ApplicationController {

    private final ApplicationApplicationService applicationService;

    // === Application CRUD ===

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create application",
            description = "Application secret is returned only once at creation time")
    public ApiResponse<ApplicationCreatedResponse> createApplication(
            @Valid @RequestBody CreateApplicationRequest request) {
        return ApiResponse.created(applicationService.createApplication(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get application by ID")
    public ApiResponse<ApplicationResponse> getApplicationById(@PathVariable Long id) {
        return ApiResponse.success(applicationService.getApplication(id));
    }

    @GetMapping("/by-app-id/{appId}")
    @Operation(summary = "Get application by appId")
    public ApiResponse<ApplicationResponse> getApplicationByAppId(@PathVariable String appId) {
        return ApiResponse.success(applicationService.getApplicationByAppId(appId));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "List applications by tenant")
    public ApiResponse<List<ApplicationResponse>> getApplicationsByTenant(
            @PathVariable Long tenantId) {
        return ApiResponse.success(applicationService.getApplicationsByTenantId(tenantId));
    }

    @GetMapping
    @Operation(summary = "List all applications")
    public ApiResponse<List<ApplicationResponse>> listAllApplications() {
        return ApiResponse.success(applicationService.listAllApplications());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update application")
    public ApiResponse<ApplicationResponse> updateApplication(@PathVariable Long id,
            @Valid @RequestBody UpdateApplicationRequest request) {
        return ApiResponse.success(applicationService.updateApplication(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete application")
    public void deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
    }

    @PostMapping("/{id}/rotate-secret")
    @Operation(summary = "Rotate application secret",
            description = "New secret is returned only once")
    public ApiResponse<ApplicationCreatedResponse> rotateSecret(@PathVariable Long id) {
        return ApiResponse.success(applicationService.rotateAppSecret(id));
    }

    // === Application Status Management ===

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate application")
    public ApiResponse<Void> activateApplication(@PathVariable Long id) {
        applicationService.activateApplication(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate application")
    public ApiResponse<Void> deactivateApplication(@PathVariable Long id) {
        applicationService.deactivateApplication(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/block")
    @Operation(summary = "Block application")
    public ApiResponse<Void> blockApplication(@PathVariable Long id) {
        applicationService.blockApplication(id);
        return ApiResponse.success(null);
    }

    // === Application Permission Management ===

    @PostMapping("/{id}/permissions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create application permission")
    public ApiResponse<ApplicationPermissionResponse> createPermission(@PathVariable Long id,
            @Valid @RequestBody CreateApplicationPermissionRequest request) {
        return ApiResponse.created(applicationService.createPermission(id, request));
    }

    @GetMapping("/{id}/permissions")
    @Operation(summary = "List application permissions")
    public ApiResponse<List<ApplicationPermissionResponse>> getPermissions(@PathVariable Long id) {
        return ApiResponse.success(applicationService.getPermissionsByApplicationId(id));
    }

    @DeleteMapping("/permissions/{permissionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete application permission")
    public void deletePermission(@PathVariable Long permissionId) {
        applicationService.deletePermission(permissionId);
    }
}
