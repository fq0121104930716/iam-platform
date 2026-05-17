package iam.platform.admin.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import iam.platform.common.dto.request.CreateTenantAccountRequest;
import iam.platform.common.dto.request.UpdateTenantAccountRequest;
import iam.platform.common.dto.response.TenantAccountResponse;
import iam.platform.admin.application.service.TenantAccountApplicationService;
import iam.platform.common.api.ApiResponse;
import iam.platform.common.api.PageResponse;

import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "TenantAccount", description = "Tenant account management API")
public class TenantAccountController {

    private final TenantAccountApplicationService tenantAccountApplicationService;

    @PostMapping("/users/{userId}/tenant-accounts")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create tenant account for User")
    public ApiResponse<TenantAccountResponse> create(@PathVariable Long userId,
            @Valid @RequestBody CreateTenantAccountRequest request) {
        return ApiResponse
                .created(tenantAccountApplicationService.createTenantAccount(userId, request));
    }

    @GetMapping("/tenant-accounts/{id}")
    @Operation(summary = "Get tenant account by ID")
    public ApiResponse<TenantAccountResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(tenantAccountApplicationService.getTenantAccount(id));
    }

    @PutMapping("/tenant-accounts/{id}")
    @Operation(summary = "Update tenant account")
    public ApiResponse<TenantAccountResponse> update(@PathVariable Long id,
            @Valid @RequestBody UpdateTenantAccountRequest request) {
        return ApiResponse
                .success(tenantAccountApplicationService.updateTenantAccount(id, request));
    }

    @PostMapping("/tenant-accounts/{id}/suspend")
    @Operation(summary = "Suspend tenant account")
    public ApiResponse<Void> suspend(@PathVariable Long id) {
        tenantAccountApplicationService.suspendTenantAccount(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/tenant-accounts/{id}/reactivate")
    @Operation(summary = "Reactivate tenant account")
    public ApiResponse<Void> reactivate(@PathVariable Long id) {
        tenantAccountApplicationService.reactivateTenantAccount(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/tenant-accounts/{id}/leave")
    @Operation(summary = "Leave tenant")
    public ApiResponse<Void> leave(@PathVariable Long id) {
        tenantAccountApplicationService.leaveTenant(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/users/{userId}/tenant-accounts")
    @Operation(summary = "Get all tenant accounts for a User")
    public ApiResponse<List<TenantAccountResponse>> getByUserId(@PathVariable Long userId) {
        return ApiResponse
                .success(tenantAccountApplicationService.getTenantAccountsByUserId(userId));
    }

    @GetMapping("/tenants/{tenantId}/tenant-accounts")
    @Operation(summary = "List tenant accounts for a tenant")
    public ApiResponse<PageResponse<TenantAccountResponse>> listByTenant(
            @PathVariable Long tenantId, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse
                .success(tenantAccountApplicationService.listTenantAccounts(tenantId, page, size));
    }
}
