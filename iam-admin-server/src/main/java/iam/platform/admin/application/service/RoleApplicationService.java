package iam.platform.admin.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import iam.platform.common.dto.request.CreateRoleRequest;
import iam.platform.common.dto.response.RoleResponse;
import iam.platform.common.model.annotation.AuditLog;
import iam.platform.admin.domain.model.entity.Role;
import iam.platform.common.model.enums.AuditEventType;
import iam.platform.common.model.exception.RoleNotFoundException;
import iam.platform.admin.domain.repository.RoleRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleApplicationService {

    private final RoleRepository roleRepository;

    @Transactional
    @AuditLog(value = AuditEventType.ROLE_CREATED, resourceType = "role", action = "创建角色 #{#request.code}")
    public RoleResponse createRole(Long tenantId, CreateRoleRequest request) {
        // Check uniqueness within tenant
        if (tenantId != null
                && roleRepository.existsByTenantIdAndCode(tenantId, request.getCode())) {
            throw new IllegalArgumentException(
                    "Role code already exists in this tenant: " + request.getCode());
        }
        if (tenantId == null && roleRepository.findByCode(request.getCode()).isPresent()) {
            throw new IllegalArgumentException(
                    "Global role code already exists: " + request.getCode());
        }

        Role role = Role.create(tenantId, request.getCode(), request.getName(),
                request.getRoleType(), request.getDescription(), request.getIsSystem());
        role = roleRepository.save(role);
        log.info("Role created: {} for tenant: {}", request.getCode(), tenantId);
        return toResponse(role);
    }

    public RoleResponse getRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role not found: " + id));
        return toResponse(role);
    }

    public List<RoleResponse> listRoles(Long tenantId) {
        List<Role> roles;
        if (tenantId != null) {
            // Return tenant roles + global roles
            roles = roleRepository.findByTenantIdOrGlobal(tenantId);
        } else {
            roles = roleRepository.findAll();
        }
        return roles.stream().map(this::toResponse).toList();
    }

    @Transactional
    @AuditLog(value = AuditEventType.ROLE_DELETED, resourceType = "role", action = "删除角色 ID=#{#id}")
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role not found: " + id));

        // System roles cannot be deleted
        if (!role.canBeDeleted()) {
            throw new IllegalStateException("System roles cannot be deleted: " + role.getCode());
        }

        roleRepository.deleteById(id);
        log.info("Role deleted: {}", id);
    }

    private RoleResponse toResponse(Role role) {
        return RoleResponse.builder().id(role.getId()).tenantId(role.getTenantId())
                .code(role.getCode()).name(role.getName()).roleType(role.getRoleType())
                .description(role.getDescription()).isSystem(role.getIsSystem())
                .createdAt(role.getCreatedAt()).updatedAt(role.getUpdatedAt()).build();
    }
}
