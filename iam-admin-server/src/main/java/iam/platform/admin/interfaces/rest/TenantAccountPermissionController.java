package iam.platform.admin.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import iam.platform.common.dto.response.PermissionResponse;
import iam.platform.common.dto.response.RoleResponse;
import iam.platform.admin.application.service.TenantAccountRoleApplicationService;
import iam.platform.admin.domain.service.PermissionEvaluationService;
import iam.platform.common.api.ApiResponse;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "TenantAccountPermission",
        description = "Tenant account role assignment and permission query API")
public class TenantAccountPermissionController {

    private final TenantAccountRoleApplicationService tenantAccountRoleApplicationService;
    private final PermissionEvaluationService permissionEvaluationService;

    @PostMapping("/tenant-accounts/{accountId}/roles")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Assign role to tenant account")
    public ApiResponse<Void> assignRole(@PathVariable Long accountId, @RequestParam Long roleId) {
        tenantAccountRoleApplicationService.assignRoleToTenantAccount(accountId, roleId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/tenant-accounts/{accountId}/roles")
    @Operation(summary = "Remove role from tenant account")
    public ApiResponse<Void> removeRole(@PathVariable Long accountId, @RequestParam Long roleId) {
        tenantAccountRoleApplicationService.removeRoleFromTenantAccount(accountId, roleId);
        return ApiResponse.success(null);
    }

    @GetMapping("/tenant-accounts/{accountId}/roles")
    @Operation(summary = "Get roles of tenant account")
    public ApiResponse<List<RoleResponse>> getTenantAccountRoles(@PathVariable Long accountId) {
        List<RoleResponse> roles =
                tenantAccountRoleApplicationService.getTenantAccountRoles(accountId);
        return ApiResponse.success(roles);
    }

    @GetMapping("/tenant-accounts/{accountId}/permissions")
    @Operation(summary = "Get permissions of tenant account")
    public ApiResponse<List<PermissionResponse>> getTenantAccountPermissions(
            @PathVariable Long accountId) {
        List<PermissionResponse> permissions =
                tenantAccountRoleApplicationService.getTenantAccountPermissions(accountId);
        return ApiResponse.success(permissions);
    }

    @PostMapping("/permissions/check")
    @Operation(summary = "Check if tenant account has specific permission")
    public ApiResponse<Boolean> checkPermission(@RequestParam Long accountId,
            @RequestParam String permissionCode) {
        boolean hasPermission =
                permissionEvaluationService.hasPermission(accountId, permissionCode);
        return ApiResponse.success(hasPermission);
    }

    @PostMapping("/permissions/check/batch")
    @Operation(summary = "Batch check permissions")
    public ApiResponse<Set<String>> batchCheckPermissions(@RequestParam Long accountId,
            @RequestParam Set<String> permissionCodes) {
        Set<String> userPermissions = permissionEvaluationService.getAllPermissions(accountId);
        Set<String> grantedPermissions = userPermissions.stream().filter(permissionCodes::contains)
                .collect(java.util.stream.Collectors.toSet());
        return ApiResponse.success(grantedPermissions);
    }
}
