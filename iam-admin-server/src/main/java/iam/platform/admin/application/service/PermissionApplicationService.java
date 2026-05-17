package iam.platform.admin.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import iam.platform.common.dto.request.CreateResourcePermissionRequest;
import iam.platform.common.dto.response.ResourcePermissionResponse;
import iam.platform.common.dto.response.RolePermissionResponse;
import iam.platform.common.model.annotation.AuditLog;
import iam.platform.admin.domain.model.entity.ResourcePermission;
import iam.platform.admin.domain.model.entity.RolePermission;
import iam.platform.common.model.enums.AuditEventType;
import iam.platform.common.model.enums.PermissionAction;
import iam.platform.common.model.exception.RoleNotFoundException;
import iam.platform.admin.domain.repository.ResourcePermissionRepository;
import iam.platform.admin.domain.repository.RolePermissionRepository;
import iam.platform.admin.domain.repository.RoleRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionApplicationService {

    private final ResourcePermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final RoleRepository roleRepository;

    @Transactional
    @AuditLog(value = AuditEventType.PERMISSION_CREATED, resourceType = "permission", action = "创建权限 #{#request.permissionCode}")
    public ResourcePermissionResponse createPermission(Long tenantId,
            CreateResourcePermissionRequest request) {
        // Check uniqueness within tenant (FIX: tenant-scoped, not global)
        if (permissionRepository.existsByTenantIdAndPermissionCode(tenantId,
                request.getPermissionCode())) {
            throw new IllegalArgumentException(
                    "Permission code already exists in this tenant: " + request.getPermissionCode());
        }

        PermissionAction action = PermissionAction.valueOf(request.getAction().name());
        ResourcePermission permission = ResourcePermission.create(tenantId,
                request.getResourceType(), action, request.getPermissionName(),
                request.getDescription());
        permission = permissionRepository.save(permission);
        log.info("Permission created: {} for tenant: {}", permission.getPermissionCode(), tenantId);
        return toResponse(permission);
    }

    public ResourcePermissionResponse getPermission(Long id) {
        ResourcePermission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + id));
        return toResponse(permission);
    }

    public List<ResourcePermissionResponse> listPermissions(Long tenantId, String resourceType) {
        List<ResourcePermission> permissions;
        if (resourceType != null) {
            permissions = permissionRepository.findByResourceType(resourceType);
        } else if (tenantId != null) {
            permissions = permissionRepository.findByTenantIdOrGlobal(tenantId);
        } else {
            permissions = permissionRepository.findGlobalPermissions();
        }
        return permissions.stream().map(this::toResponse).toList();
    }

    @Transactional
    @AuditLog(value = AuditEventType.PERMISSION_DELETED, resourceType = "permission", action = "删除权限 ID=#{#id}")
    public void deletePermission(Long id) {
        if (!permissionRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("Permission not found: " + id);
        }
        // Delete role-permission mappings first
        rolePermissionRepository.deleteByPermissionId(id);
        permissionRepository.deleteById(id);
        log.info("Permission deleted: {}", id);
    }

    @Transactional
    public RolePermissionResponse assignPermissionToRole(Long roleId, Long permissionId) {
        // Verify role exists
        if (!roleRepository.findById(roleId).isPresent()) {
            throw new RoleNotFoundException("Role not found: " + roleId);
        }

        // Verify permission exists
        if (!permissionRepository.findById(permissionId).isPresent()) {
            throw new IllegalArgumentException("Permission not found: " + permissionId);
        }

        // Check if already assigned
        if (rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            throw new IllegalArgumentException("Permission already assigned to role");
        }

        RolePermission rolePermission = RolePermission.builder().roleId(roleId).permissionId(permissionId).build();
        rolePermission = rolePermissionRepository.save(rolePermission);
        log.info("Permission {} assigned to role {}", permissionId, roleId);
        return toRolePermissionResponse(rolePermission);
    }

    @Transactional
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        if (!rolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            throw new IllegalArgumentException("Permission not assigned to role");
        }
        rolePermissionRepository.deleteByRoleIdAndPermissionId(roleId, permissionId);
        log.info("Permission {} removed from role {}", permissionId, roleId);
    }

    public List<ResourcePermissionResponse> getPermissionsByRole(Long roleId) {
        List<RolePermission> rolePermissions = rolePermissionRepository.findByRoleId(roleId);
        return rolePermissions.stream()
                .map(rp -> permissionRepository.findById(rp.getPermissionId()))
                .filter(opt -> opt.isPresent()).map(opt -> toResponse(opt.get())).toList();
    }

    private ResourcePermissionResponse toResponse(ResourcePermission permission) {
        return ResourcePermissionResponse.builder().id(permission.getId())
                .tenantId(permission.getTenantId()).permissionCode(permission.getPermissionCode())
                .permissionName(permission.getPermissionName())
                .resourceType(permission.getResourceType()).action(permission.getAction())
                .description(permission.getDescription()).createdAt(permission.getCreatedAt())
                .updatedAt(permission.getUpdatedAt()).build();
    }

    private RolePermissionResponse toRolePermissionResponse(RolePermission rolePermission) {
        return RolePermissionResponse.builder().id(rolePermission.getId())
                .roleId(rolePermission.getRoleId()).permissionId(rolePermission.getPermissionId())
                .createdAt(rolePermission.getCreatedAt()).build();
    }
}
