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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import iam.platform.common.dto.request.CreateTenantRequest;
import iam.platform.common.dto.request.UpdateTenantRequest;
import iam.platform.common.dto.response.TenantResponse;
import iam.platform.admin.application.service.TenantApplicationService;
import iam.platform.common.model.annotation.RequirePermission;
import iam.platform.common.api.ApiResponse;
import iam.platform.common.api.PageResponse;

@RestController
@RequestMapping("/v1/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenant", description = "Tenant management API")
public class TenantController {

    private final TenantApplicationService tenantApplicationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new tenant")
    @RequirePermission("tenant:write")
    public ApiResponse<TenantResponse> create(@Valid @RequestBody CreateTenantRequest request) {
        return ApiResponse.created(tenantApplicationService.createTenant(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tenant by ID")
    @RequirePermission("tenant:read")
    public ApiResponse<TenantResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(tenantApplicationService.getTenant(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update tenant")
    @RequirePermission("tenant:write")
    public ApiResponse<TenantResponse> update(@PathVariable Long id,
            @Valid @RequestBody UpdateTenantRequest request) {
        return ApiResponse.success(tenantApplicationService.updateTenant(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete tenant (soft delete)")
    @RequirePermission("tenant:write")
    public void delete(@PathVariable Long id) {
        tenantApplicationService.deleteTenant(id);
    }

    @GetMapping
    @Operation(summary = "List tenants")
    @RequirePermission("tenant:read")
    public ApiResponse<PageResponse<TenantResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(tenantApplicationService.listTenants(page, size));
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate tenant")
    @RequirePermission("tenant:write")
    public ApiResponse<Void> activate(@PathVariable Long id) {
        tenantApplicationService.activateTenant(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/suspend")
    @Operation(summary = "Suspend tenant")
    @RequirePermission("tenant:write")
    public ApiResponse<Void> suspend(@PathVariable Long id) {
        tenantApplicationService.suspendTenant(id);
        return ApiResponse.success(null);
    }
}
