package iam.platform.admin.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import iam.platform.admin.application.service.UserTenantApplicationService;
import iam.platform.admin.application.service.UserTenantApplicationService.UserTenantMappingResponse;
import iam.platform.admin.application.service.UserTenantApplicationService.UserRoleMappingResponse;
import iam.platform.common.api.ApiResponse;
import iam.platform.common.api.PageResponse;

import java.util.List;

@RestController
@RequestMapping("/v1/user-tenants")
@RequiredArgsConstructor
@Tag(name = "User-Tenant Mapping", description = "User-Tenant association management API")
public class UserTenantController {

    private final UserTenantApplicationService userTenantApplicationService;

    @PostMapping("/{userId}/tenants/{tenantId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Associate user with tenant")
    public ApiResponse<UserTenantMappingResponse> createMapping(@PathVariable Long userId,
            @PathVariable Long tenantId, @RequestParam(required = false) String accountCode,
            @RequestParam(required = false) String employeeNo) {
        return ApiResponse.created(userTenantApplicationService.createMapping(userId, tenantId,
                accountCode, employeeNo));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user-tenant mapping by ID")
    public ApiResponse<UserTenantMappingResponse> getMapping(@PathVariable Long id) {
        return ApiResponse.success(userTenantApplicationService.getMapping(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get tenant mappings by user ID")
    public ApiResponse<List<UserTenantMappingResponse>> getByUserId(@PathVariable Long userId) {
        return ApiResponse.success(userTenantApplicationService.getByUserId(userId));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "List user mappings by tenant ID")
    public ApiResponse<PageResponse<UserTenantMappingResponse>> getByTenantId(
            @PathVariable Long tenantId, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse
                .success(userTenantApplicationService.getByTenantId(tenantId, page, size));
    }

    @PutMapping("/{id}/suspend")
    @Operation(summary = "Suspend user-tenant mapping")
    public ApiResponse<Void> suspendMapping(@PathVariable Long id) {
        userTenantApplicationService.suspendMapping(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/reactivate")
    @Operation(summary = "Reactivate user-tenant mapping")
    public ApiResponse<Void> reactivateMapping(@PathVariable Long id) {
        userTenantApplicationService.reactivateMapping(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/{id}/leave")
    @Operation(summary = "User leaves tenant")
    public ApiResponse<Void> leaveTenant(@PathVariable Long id) {
        userTenantApplicationService.leaveTenant(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/roles")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Assign role to user in tenant")
    public ApiResponse<Void> assignRole(@RequestParam Long userId, @RequestParam Long tenantId,
            @RequestParam Long roleId, @RequestParam(required = false) Long assignedBy) {
        userTenantApplicationService.assignRole(userId, tenantId, roleId, assignedBy);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/roles")
    @Operation(summary = "Revoke role from user in tenant")
    public ApiResponse<Void> revokeRole(@RequestParam Long userId, @RequestParam Long tenantId,
            @RequestParam Long roleId) {
        userTenantApplicationService.revokeRole(userId, tenantId, roleId);
        return ApiResponse.success(null);
    }

    @GetMapping("/roles/user/{userId}/tenant/{tenantId}")
    @Operation(summary = "Get user roles in tenant")
    public ApiResponse<List<UserRoleMappingResponse>> getUserRoles(@PathVariable Long userId,
            @PathVariable Long tenantId) {
        return ApiResponse.success(userTenantApplicationService.getUserRoles(userId, tenantId));
    }
}
