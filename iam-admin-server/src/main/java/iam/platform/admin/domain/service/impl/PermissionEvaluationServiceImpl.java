package iam.platform.admin.domain.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import iam.platform.admin.application.service.TenantAccountRoleApplicationService;
import iam.platform.common.model.exception.AccessDeniedException;
import iam.platform.admin.domain.service.PermissionEvaluationService;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionEvaluationServiceImpl implements PermissionEvaluationService {

    private final TenantAccountRoleApplicationService tenantAccountRoleApplicationService;

    @Override
    public boolean hasPermission(Long tenantAccountId, String permissionCode) {
        Set<String> permissions = getAllPermissions(tenantAccountId);
        return permissions.contains(permissionCode);
    }

    @Override
    public boolean hasAnyPermission(Long tenantAccountId, Set<String> permissionCodes) {
        Set<String> userPermissions = getAllPermissions(tenantAccountId);
        return permissionCodes.stream().anyMatch(userPermissions::contains);
    }

    @Override
    public boolean hasAllPermissions(Long tenantAccountId, Set<String> permissionCodes) {
        Set<String> userPermissions = getAllPermissions(tenantAccountId);
        return permissionCodes.stream().allMatch(userPermissions::contains);
    }

    @Override
    @Cacheable(value = "permissions", key = "#tenantAccountId")
    public Set<String> getAllPermissions(Long tenantAccountId) {
        return tenantAccountRoleApplicationService.getAllPermissionCodes(tenantAccountId);
    }

    @Override
    public void checkPermission(Long tenantAccountId, String permissionCode) {
        if (!hasPermission(tenantAccountId, permissionCode)) {
            log.warn("Access denied: tenant account {} lacks permission {}", tenantAccountId,
                    permissionCode);
            throw new AccessDeniedException(permissionCode);
        }
    }
}
