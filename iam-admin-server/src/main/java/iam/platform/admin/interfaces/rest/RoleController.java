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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import iam.platform.common.dto.request.CreateRoleRequest;
import iam.platform.common.dto.response.RoleResponse;
import iam.platform.admin.application.service.RoleApplicationService;
import iam.platform.common.api.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/v1/tenants/{tenantId}/roles")
@RequiredArgsConstructor
@Tag(name = "Role", description = "Role management API")
public class RoleController {

    private final RoleApplicationService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create tenant role")
    public ApiResponse<RoleResponse> create(@PathVariable Long tenantId,
            @Valid @RequestBody CreateRoleRequest request) {
        return ApiResponse.created(service.createRole(tenantId, request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID")
    public ApiResponse<RoleResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getRole(id));
    }

    @GetMapping
    @Operation(summary = "List tenant roles (includes global roles)")
    public ApiResponse<List<RoleResponse>> list(@PathVariable Long tenantId) {
        return ApiResponse.success(service.listRoles(tenantId));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete role")
    public void delete(@PathVariable Long id) {
        service.deleteRole(id);
    }
}
