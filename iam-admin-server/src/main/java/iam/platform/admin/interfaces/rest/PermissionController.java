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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import iam.platform.common.dto.request.CreateResourcePermissionRequest;
import iam.platform.common.dto.response.ResourcePermissionResponse;
import iam.platform.common.dto.response.RolePermissionResponse;
import iam.platform.admin.application.service.PermissionApplicationService;
import iam.platform.common.api.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "Permission", description = "Resource permission management API")
public class PermissionController {

    private final PermissionApplicationService permissionService;

    // ========== Permission Management ==========

    @PostMapping("/tenants/{tenantId}/permissions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create resource permission")
    public ApiResponse<ResourcePermissionResponse> createPermission(@PathVariable Long tenantId,
            @Valid @RequestBody CreateResourcePermissionRequest request) {
        return ApiResponse.created(permissionService.createPermission(tenantId, request));
    }

    @GetMapping("/permissions/{id}")
    @Operation(summary = "Get permission by ID")
    public ApiResponse<ResourcePermissionResponse> getPermission(@PathVariable Long id) {
        return ApiResponse.success(permissionService.getPermission(id));
    }

    @GetMapping("/permissions")
    @Operation(summary = "List permissions (supports filtering)")
    public ApiResponse<List<ResourcePermissionResponse>> listPermissions(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) String resourceType) {
        return ApiResponse.success(permissionService.listPermissions(tenantId, resourceType));
    }

    @DeleteMapping("/permissions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete permission")
    public void deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
    }

    // ========== Role-Permission Assignment ==========

    @PostMapping("/roles/{roleId}/permissions")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Assign permission to role")
    public ApiResponse<RolePermissionResponse> assignPermissionToRole(@PathVariable Long roleId,
            @RequestParam Long permissionId) {
        return ApiResponse.created(permissionService.assignPermissionToRole(roleId, permissionId));
    }

    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove permission from role")
    public void removePermissionFromRole(@PathVariable Long roleId,
            @PathVariable Long permissionId) {
        permissionService.removePermissionFromRole(roleId, permissionId);
    }

    @GetMapping("/roles/{roleId}/permissions")
    @Operation(summary = "Get permissions by role")
    public ApiResponse<List<ResourcePermissionResponse>> getPermissionsByRole(
            @PathVariable Long roleId) {
        return ApiResponse.success(permissionService.getPermissionsByRole(roleId));
    }
}
