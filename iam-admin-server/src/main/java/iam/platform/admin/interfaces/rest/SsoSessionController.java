package iam.platform.admin.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import iam.platform.admin.application.service.SsoSessionService;
import iam.platform.admin.application.service.SsoSessionService.TenantAccountResponse;
import iam.platform.admin.application.service.SsoSessionService.TenantSwitchResponse;
import iam.platform.common.api.ApiResponse;

import java.util.List;

/**
 * REST controller for SSO session management. Provides APIs for tenant
 * selection, switching, and
 * session management.
 */
@RestController
@RequestMapping("/v1/sso")
@RequiredArgsConstructor
@Tag(name = "SSO Session", description = "Multi-tenant SSO session management APIs")
public class SsoSessionController {

    private final SsoSessionService ssoSessionService;

    @GetMapping("/tenants")
    @Operation(summary = "Get available tenants for current user", description = "Returns all active tenant accounts that the authenticated user belongs to")
    public ApiResponse<List<TenantAccountResponse>> getAvailableTenants() {
        List<TenantAccountResponse> tenants = ssoSessionService.getAvailableTenants();
        return ApiResponse.success(tenants);
    }

    @PostMapping("/tenants/{tenantAccountId}/switch")
    @Operation(summary = "Switch tenant context", description = "Switches the current tenant context for the authenticated user. "
            + "This is used when a user belongs to multiple tenants and wants to switch between them.")
    public ApiResponse<TenantSwitchResponse> switchTenant(@PathVariable Long tenantAccountId) {
        TenantSwitchResponse response = ssoSessionService.switchTenant(tenantAccountId);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/tenant-context")
    @Operation(summary = "Clear tenant context", description = "Clears the current tenant context. User will need to select a tenant again.")
    public ApiResponse<Void> clearTenantContext() {
        ssoSessionService.clearTenantContext();
        return ApiResponse.success(null);
    }
}
