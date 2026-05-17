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
import iam.platform.common.dto.request.CreateOrganizationRequest;
import iam.platform.common.dto.request.UpdateOrganizationRequest;
import iam.platform.common.dto.response.OrganizationResponse;
import iam.platform.admin.application.service.OrganizationApplicationService;
import iam.platform.common.api.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/v1/tenants/{tenantId}/organizations")
@RequiredArgsConstructor
@Tag(name = "Organization", description = "Organization management API")
public class OrganizationController {

    private final OrganizationApplicationService organizationApplicationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create organization")
    public ApiResponse<OrganizationResponse> create(@PathVariable Long tenantId,
            @Valid @RequestBody CreateOrganizationRequest request) {
        return ApiResponse
                .created(organizationApplicationService.createOrganization(tenantId, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization by ID")
    public ApiResponse<OrganizationResponse> getById(@PathVariable Long tenantId,
            @PathVariable Long id) {
        return ApiResponse.success(organizationApplicationService.getOrganization(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update organization")
    public ApiResponse<OrganizationResponse> update(@PathVariable Long tenantId,
            @PathVariable Long id, @Valid @RequestBody UpdateOrganizationRequest request) {
        return ApiResponse
                .success(organizationApplicationService.updateOrganization(tenantId, id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete organization")
    public void delete(@PathVariable Long tenantId, @PathVariable Long id) {
        organizationApplicationService.deleteOrganization(tenantId, id);
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate organization")
    public ApiResponse<Void> activate(@PathVariable Long tenantId, @PathVariable Long id) {
        organizationApplicationService.activateOrganization(tenantId, id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate organization")
    public ApiResponse<Void> deactivate(@PathVariable Long tenantId, @PathVariable Long id) {
        organizationApplicationService.deactivateOrganization(tenantId, id);
        return ApiResponse.success(null);
    }

    @GetMapping
    @Operation(summary = "Get organization tree for tenant")
    public ApiResponse<List<OrganizationResponse>> getTree(@PathVariable Long tenantId) {
        return ApiResponse.success(organizationApplicationService.getOrganizationTree(tenantId));
    }
}
