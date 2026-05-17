package iam.platform.admin.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import iam.platform.common.dto.request.AssignOrganizationRequest;
import iam.platform.common.dto.response.OrganizationResponse;
import iam.platform.common.dto.response.TenantAccountResponse;
import iam.platform.admin.application.service.TenantAccountOrganizationApplicationService;
import iam.platform.common.api.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "TenantAccountOrganization", description = "Tenant account organization assignment API")
public class TenantAccountOrganizationController {

    private final TenantAccountOrganizationApplicationService tenantAccountOrganizationApplicationService;

    @PostMapping("/tenant-accounts/{accountId}/organizations")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Assign tenant account to organization")
    public ApiResponse<Void> assignTenantAccountToOrganization(@PathVariable Long accountId,
            @RequestBody AssignOrganizationRequest request) {
        tenantAccountOrganizationApplicationService.assignTenantAccountToOrganization(accountId,
                request.getOrganizationId(), request.getIsPrimary(), request.getPosition());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/tenant-accounts/{accountId}/organizations/{orgId}")
    @Operation(summary = "Remove tenant account from organization")
    public ApiResponse<Void> removeTenantAccountFromOrganization(@PathVariable Long accountId,
            @PathVariable Long orgId) {
        tenantAccountOrganizationApplicationService.removeTenantAccountFromOrganization(accountId,
                orgId);
        return ApiResponse.success(null);
    }

    @GetMapping("/tenant-accounts/{accountId}/organizations")
    @Operation(summary = "Get tenant account's organizations")
    public ApiResponse<List<OrganizationResponse>> getTenantAccountOrganizations(
            @PathVariable Long accountId) {
        List<OrganizationResponse> organizations = tenantAccountOrganizationApplicationService
                .getTenantAccountOrganizations(accountId);
        return ApiResponse.success(organizations);
    }

    @PutMapping("/tenant-accounts/{accountId}/organizations/{orgId}/primary")
    @Operation(summary = "Set primary organization")
    public ApiResponse<Void> setPrimaryOrganization(@PathVariable Long accountId,
            @PathVariable Long orgId) {
        tenantAccountOrganizationApplicationService.setPrimaryOrganization(accountId, orgId);
        return ApiResponse.success(null);
    }

    @GetMapping("/organizations/{orgId}/tenant-accounts")
    @Operation(summary = "Get organization members")
    public ApiResponse<List<TenantAccountResponse>> getOrganizationMembers(
            @PathVariable Long orgId) {
        List<TenantAccountResponse> members =
                tenantAccountOrganizationApplicationService.getOrganizationMembers(orgId);
        return ApiResponse.success(members);
    }
}
