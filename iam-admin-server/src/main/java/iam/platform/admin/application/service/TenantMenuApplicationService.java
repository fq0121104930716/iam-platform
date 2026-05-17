package iam.platform.admin.application.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import iam.platform.admin.domain.model.entity.PlatformMenu;
import iam.platform.admin.domain.model.entity.TenantMenuConfig;
import iam.platform.admin.domain.repository.PlatformMenuRepository;
import iam.platform.admin.domain.repository.TenantMenuConfigRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantMenuApplicationService {

    private final PlatformMenuRepository platformMenuRepository;
    private final TenantMenuConfigRepository tenantMenuConfigRepository;

    // ==================== Platform Menu Management ====================

    @Transactional
    public PlatformMenuResponse createMenu(String menuCode, String menuName, String icon,
            String path, Integer sortOrder, Long parentId, String description) {
        PlatformMenu menu = PlatformMenu.create(menuCode, menuName, icon, path, sortOrder, parentId,
                description);
        menu = platformMenuRepository.save(menu);
        log.info("Platform menu created: {}", menuCode);
        return toMenuResponse(menu);
    }

    public PlatformMenuResponse getMenu(Long id) {
        PlatformMenu menu = platformMenuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu not found: " + id));
        return toMenuResponse(menu);
    }

    public List<PlatformMenuResponse> listAllMenus() {
        return platformMenuRepository.findAllOrderBySortOrder().stream().map(this::toMenuResponse)
                .toList();
    }

    @Transactional
    public PlatformMenuResponse updateMenu(Long id, String menuName, String icon, String path,
            Integer sortOrder, String description) {
        PlatformMenu menu = platformMenuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu not found: " + id));
        menu.update(menuName, icon, path, sortOrder, description);
        menu = platformMenuRepository.save(menu);
        return toMenuResponse(menu);
    }

    @Transactional
    public void deleteMenu(Long id) {
        platformMenuRepository.deleteById(id);
        log.info("Platform menu deleted: {}", id);
    }

    // ==================== Tenant Menu Configuration ====================

    @Transactional
    public TenantMenuConfigResponse configureMenu(Long tenantId, Long menuId, Boolean enabled) {
        var existing = tenantMenuConfigRepository.findByTenantIdAndMenuId(tenantId, menuId);
        if (existing.isPresent()) {
            TenantMenuConfig config = existing.get();
            if (Boolean.TRUE.equals(enabled)) {
                config.enable();
            } else {
                config.disable();
            }
            config = tenantMenuConfigRepository.save(config);
            return toConfigResponse(config);
        }

        TenantMenuConfig config = TenantMenuConfig.create(tenantId, menuId, enabled);
        config = tenantMenuConfigRepository.save(config);
        log.info("Tenant menu configured: tenantId={}, menuId={}", tenantId, menuId);
        return toConfigResponse(config);
    }

    public List<TenantMenuConfigResponse> getTenantMenus(Long tenantId) {
        return tenantMenuConfigRepository.findByTenantId(tenantId).stream()
                .map(this::toConfigResponse).toList();
    }

    public List<TenantMenuConfigResponse> getEnabledTenantMenus(Long tenantId) {
        return tenantMenuConfigRepository.findByTenantIdAndEnabledTrue(tenantId).stream()
                .map(this::toConfigResponse).toList();
    }

    @Transactional
    public void removeTenantMenu(Long tenantId, Long menuId) {
        tenantMenuConfigRepository.deleteByTenantIdAndMenuId(tenantId, menuId);
        log.info("Tenant menu removed: tenantId={}, menuId={}", tenantId, menuId);
    }

    private PlatformMenuResponse toMenuResponse(PlatformMenu menu) {
        return PlatformMenuResponse.builder().id(menu.getId()).menuCode(menu.getMenuCode())
                .menuName(menu.getMenuName()).icon(menu.getIcon()).path(menu.getPath())
                .sortOrder(menu.getSortOrder()).parentId(menu.getParentId())
                .description(menu.getDescription()).createdAt(menu.getCreatedAt())
                .updatedAt(menu.getUpdatedAt()).build();
    }

    private TenantMenuConfigResponse toConfigResponse(TenantMenuConfig config) {
        return TenantMenuConfigResponse.builder().id(config.getId()).tenantId(config.getTenantId())
                .menuId(config.getMenuId()).enabled(config.isEnabled())
                .createdAt(config.getCreatedAt()).build();
    }

    @Builder
    public static class PlatformMenuResponse {
        public Long id;
        public String menuCode, menuName, icon, path, description;
        public Integer sortOrder;
        public Long parentId;
        public java.time.LocalDateTime createdAt, updatedAt;
    }

    @Builder
    public static class TenantMenuConfigResponse {
        public Long id, tenantId, menuId;
        public Boolean enabled;
        public java.time.LocalDateTime createdAt;
    }
}
