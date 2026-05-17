package iam.platform.admin.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import iam.platform.common.dto.response.PermissionResponse;
import iam.platform.common.dto.response.RoleResponse;
import iam.platform.common.model.annotation.AuditLog;
import iam.platform.admin.domain.model.entity.ResourcePermission;
import iam.platform.admin.domain.model.entity.Role;
import iam.platform.admin.domain.model.entity.RolePermission;
import iam.platform.admin.domain.model.entity.TenantAccountRoleMapping;
import iam.platform.common.model.enums.AuditEventType;
import iam.platform.common.model.exception.RoleNotFoundException;
import iam.platform.admin.domain.repository.ResourcePermissionRepository;
import iam.platform.admin.domain.repository.RolePermissionRepository;
import iam.platform.admin.domain.repository.RoleRepository;
import iam.platform.admin.domain.repository.TenantAccountRepository;
import iam.platform.admin.domain.repository.TenantAccountRoleMappingRepository;
import iam.platform.admin.infrastructure.security.TenantContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantAccountRoleApplicationService {

    private final TenantAccountRepository tenantAccountRepository;
    private final TenantAccountRoleMappingRepository roleMappingRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final ResourcePermissionRepository permissionRepository;

    /**
     * 为租户账号分配角色
     */
    @Transactional
    @CacheEvict(value = "permissions", key = "#tenantAccountId")
    @AuditLog(value = AuditEventType.ROLE_ASSIGN, resourceType = "role",
            action = "分配角色 租户账号ID=#{#tenantAccountId} 角色ID=#{#roleId}")
    public void assignRoleToTenantAccount(Long tenantAccountId, Long roleId) {
        // 验证租户账号存在
        tenantAccountRepository.findById(tenantAccountId).orElseThrow(
                () -> new IllegalArgumentException("Tenant account not found: " + tenantAccountId));

        // 验证角色存在
        roleRepository.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not found: " + roleId));

        // 检查是否已分配
        if (roleMappingRepository.existsByTenantAccountIdAndRoleId(tenantAccountId, roleId)) {
            throw new IllegalArgumentException("Role already assigned to tenant account");
        }

        // 创建角色映射
        TenantAccountRoleMapping mapping = TenantAccountRoleMapping.builder()
                .tenantAccountId(tenantAccountId).roleId(roleId).assignedAt(LocalDateTime.now())
                .assignedBy(TenantContext.getCurrentPersonId() != null
                        ? "person-" + TenantContext.getCurrentPersonId()
                        : "system")
                .build();
        roleMappingRepository.save(mapping);

        log.info("Role {} assigned to tenant account {}", roleId, tenantAccountId);
    }

    /**
     * 从租户账号移除角色
     */
    @Transactional
    @CacheEvict(value = "permissions", key = "#tenantAccountId")
    @AuditLog(value = AuditEventType.ROLE_REVOKE, resourceType = "role",
            action = "撤销角色 租户账号ID=#{#tenantAccountId} 角色ID=#{#roleId}")
    public void removeRoleFromTenantAccount(Long tenantAccountId, Long roleId) {
        if (!roleMappingRepository.existsByTenantAccountIdAndRoleId(tenantAccountId, roleId)) {
            throw new IllegalArgumentException("Role not assigned to tenant account");
        }
        roleMappingRepository.deleteByTenantAccountIdAndRoleId(tenantAccountId, roleId);
        log.info("Role {} removed from tenant account {}", roleId, tenantAccountId);
    }

    /**
     * 获取租户账号的角色列表
     */
    public List<RoleResponse> getTenantAccountRoles(Long tenantAccountId) {
        List<TenantAccountRoleMapping> mappings =
                roleMappingRepository.findByTenantAccountId(tenantAccountId);

        return mappings.stream().map(mapping -> roleRepository.findById(mapping.getRoleId()))
                .filter(opt -> opt.isPresent()).map(opt -> toRoleResponse(opt.get()))
                .collect(Collectors.toList());
    }

    /**
     * 获取租户账号的权限列表
     */
    public List<PermissionResponse> getTenantAccountPermissions(Long tenantAccountId) {
        // 获取所有角色
        List<TenantAccountRoleMapping> mappings =
                roleMappingRepository.findByTenantAccountId(tenantAccountId);

        // 收集所有角色的权限并去重
        Set<Long> permissionIds = mappings.stream().flatMap(
                mapping -> rolePermissionRepository.findByRoleId(mapping.getRoleId()).stream())
                .map(RolePermission::getPermissionId).collect(Collectors.toSet());

        // 查询权限详情
        return permissionIds.stream()
                .map(permissionId -> permissionRepository.findById(permissionId))
                .filter(opt -> opt.isPresent()).map(opt -> toPermissionResponse(opt.get()))
                .collect(Collectors.toList());
    }

    /**
     * 获取租户账号的所有权限码（用于权限校验）
     */
    public Set<String> getAllPermissionCodes(Long tenantAccountId) {
        List<PermissionResponse> permissions = getTenantAccountPermissions(tenantAccountId);
        return permissions.stream().map(PermissionResponse::getPermissionCode)
                .collect(Collectors.toSet());
    }

    private RoleResponse toRoleResponse(Role role) {
        return RoleResponse.builder().id(role.getId()).tenantId(role.getTenantId())
                .code(role.getCode()).name(role.getName()).roleType(role.getRoleType())
                .description(role.getDescription()).isSystem(role.getIsSystem())
                .createdAt(role.getCreatedAt()).updatedAt(role.getUpdatedAt()).build();
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
