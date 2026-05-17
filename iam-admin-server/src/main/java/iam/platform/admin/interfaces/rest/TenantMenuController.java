package iam.platform.admin.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import iam.platform.admin.application.service.TenantMenuApplicationService;
import iam.platform.admin.application.service.TenantMenuApplicationService.PlatformMenuResponse;
import iam.platform.admin.application.service.TenantMenuApplicationService.TenantMenuConfigResponse;
import iam.platform.common.api.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/v1/menus")
@RequiredArgsConstructor
@Tag(name = "Platform Menu", description = "Platform menu and tenant menu configuration API")
public class TenantMenuController {

    private final TenantMenuApplicationService tenantMenuApplicationService;

    // ==================== Platform Menu Management ====================

    @PostMapping("/platform")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create platform menu")
    public ApiResponse<PlatformMenuResponse> createMenu(
            @RequestParam String menuCode, @RequestParam String menuName,
            @RequestParam(required = false) String icon,
            @RequestParam(required = false) String path,
            @RequestParam(defaultValue = "0") Integer sortOrder,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) String description) {
        return ApiResponse.created(
                tenantMenuApplicationService.createMenu(menuCode, menuName, icon, path,
                        sortOrder, parentId, description));
    }

    @GetMapping("/platform/{id}")
    @Operation(summary = "Get platform menu by ID")
    public ApiResponse<PlatformMenuResponse> getMenu(@PathVariable Long id) {
        return ApiResponse.success(tenantMenuApplicationService.getMenu(id));
    }

    @GetMapping("/platform")
    @Operation(summary = "List all platform menus")
    public ApiResponse<List<PlatformMenuResponse>> listAllMenus() {
        return ApiResponse.success(tenantMenuApplicationService.listAllMenus());
    }

    @PutMapping("/platform/{id}")
    @Operation(summary = "Update platform menu")
    public ApiResponse<PlatformMenuResponse> updateMenu(@PathVariable Long id,
            @RequestParam(required = false) String menuName,
            @RequestParam(required = false) String icon,
            @RequestParam(required = false) String path,
            @RequestParam(required = false) Integer sortOrder,
            @RequestParam(required = false) String description) {
        return ApiResponse.success(
                tenantMenuApplicationService.updateMenu(id, menuName, icon, path, sortOrder, description));
    }

    @DeleteMapping("/platform/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete platform menu")
    public void deleteMenu(@PathVariable Long id) {
        tenantMenuApplicationService.deleteMenu(id);
    }

    // ==================== Tenant Menu Configuration ====================

    @PostMapping("/tenant/{tenantId}/menu/{menuId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Configure menu for tenant")
    public ApiResponse<TenantMenuConfigResponse> configureMenu(@PathVariable Long tenantId,
            @PathVariable Long menuId, @RequestParam(defaultValue = "true") Boolean enabled) {
        return ApiResponse.created(
                tenantMenuApplicationService.configureMenu(tenantId, menuId, enabled));
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get tenant menu configuration")
    public ApiResponse<List<TenantMenuConfigResponse>> getTenantMenus(@PathVariable Long tenantId) {
        return ApiResponse.success(tenantMenuApplicationService.getTenantMenus(tenantId));
    }

    @GetMapping("/tenant/{tenantId}/enabled")
    @Operation(summary = "Get enabled menus for tenant")
    public ApiResponse<List<TenantMenuConfigResponse>> getEnabledTenantMenus(
            @PathVariable Long tenantId) {
        return ApiResponse.success(tenantMenuApplicationService.getEnabledTenantMenus(tenantId));
    }

    @DeleteMapping("/tenant/{tenantId}/menu/{menuId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove menu from tenant")
    public void removeTenantMenu(@PathVariable Long tenantId, @PathVariable Long menuId) {
        tenantMenuApplicationService.removeTenantMenu(tenantId, menuId);
    }
}
