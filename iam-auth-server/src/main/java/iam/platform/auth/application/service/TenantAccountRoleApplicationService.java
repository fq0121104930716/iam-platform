package iam.platform.auth.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import iam.platform.common.dto.response.PermissionResponse;
import iam.platform.auth.domain.model.entity.ResourcePermission;
import iam.platform.auth.domain.model.entity.RolePermission;
import iam.platform.auth.domain.model.entity.TenantAccountRoleMapping;
import iam.platform.auth.domain.repository.ResourcePermissionRepository;
import iam.platform.auth.domain.repository.RolePermissionRepository;
import iam.platform.auth.domain.repository.TenantAccountRoleMappingRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Auth-specific read-only service for querying tenant account permissions. This is a simplified
 * version that only provides permission query capabilities needed by the authentication flow
 * (TokenCustomizer, TenantAwareAuthenticationFilter, etc.).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantAccountRoleApplicationService {

    private final TenantAccountRoleMappingRepository roleMappingRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final ResourcePermissionRepository permissionRepository;

    /**
     * Get permissions for a tenant account (read-only query).
     */
    public List<PermissionResponse> getTenantAccountPermissions(Long tenantAccountId) {
        List<TenantAccountRoleMapping> mappings =
                roleMappingRepository.findByTenantAccountId(tenantAccountId);

        Set<Long> permissionIds = mappings.stream().flatMap(
                mapping -> rolePermissionRepository.findByRoleId(mapping.getRoleId()).stream())
                .map(RolePermission::getPermissionId).collect(Collectors.toSet());

        return permissionIds.stream()
                .map(permissionId -> permissionRepository.findById(permissionId))
                .filter(opt -> opt.isPresent()).map(opt -> toPermissionResponse(opt.get()))
                .collect(Collectors.toList());
    }

    /**
     * Get all permission codes for a tenant account (used for authorization checks).
     */
    public Set<String> getAllPermissionCodes(Long tenantAccountId) {
        List<PermissionResponse> permissions = getTenantAccountPermissions(tenantAccountId);
        return permissions.stream().map(PermissionResponse::getPermissionCode)
                .collect(Collectors.toSet());
    }

    private PermissionResponse toPermissionResponse(ResourcePermission permission) {
        return PermissionResponse.builder().id(permission.getId())
                .tenantId(permission.getTenantId()).permissionCode(permission.getPermissionCode())
                .permissionName(permission.getPermissionName())
                .resourceType(permission.getResourceType()).action(permission.getAction())
                .description(permission.getDescription()).createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt()).build();
    }
}
