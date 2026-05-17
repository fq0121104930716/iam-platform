package iam.platform.admin.application.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import iam.platform.admin.domain.model.entity.UserTenantMapping;
import iam.platform.admin.domain.model.entity.UserRoleMapping;
import iam.platform.admin.domain.repository.UserTenantMappingRepository;
import iam.platform.admin.domain.repository.UserRoleMappingRepository;
import iam.platform.admin.domain.repository.UserRepository;
import iam.platform.admin.domain.repository.TenantRepository;
import iam.platform.common.model.exception.UserNotFoundException;
import iam.platform.common.model.exception.TenantNotFoundException;
import iam.platform.common.api.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserTenantApplicationService {

    private final UserTenantMappingRepository userTenantMappingRepository;
    private final UserRoleMappingRepository userRoleMappingRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    @Transactional
    public UserTenantMappingResponse createMapping(Long userId, Long tenantId, String accountCode,
            String employeeNo) {
        if (!userRepository.findById(userId).isPresent()) {
            throw new UserNotFoundException("User not found: " + userId);
        }
        if (!tenantRepository.findById(tenantId).isPresent()) {
            throw new TenantNotFoundException("Tenant not found: " + tenantId);
        }

        UserTenantMapping mapping = UserTenantMapping.create(userId, tenantId, accountCode,
                employeeNo, "zh-CN", "Asia/Shanghai");
        mapping = userTenantMappingRepository.save(mapping);
        log.info("User-Tenant mapping created: userId={}, tenantId={}", userId, tenantId);
        return toResponse(mapping);
    }

    public UserTenantMappingResponse getMapping(Long id) {
        UserTenantMapping mapping = userTenantMappingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mapping not found: " + id));
        return toResponse(mapping);
    }

    public List<UserTenantMappingResponse> getByUserId(Long userId) {
        return userTenantMappingRepository.findByUserId(userId).stream().map(this::toResponse)
                .toList();
    }

    public PageResponse<UserTenantMappingResponse> getByTenantId(Long tenantId, int page,
            int size) {
        Page<UserTenantMapping> mappingPage =
                userTenantMappingRepository.findByTenantId(tenantId, PageRequest.of(page, size));
        return PageResponse.of(mappingPage.getContent().stream().map(this::toResponse).toList(),
                mappingPage.getNumber(), mappingPage.getSize(), mappingPage.getTotalElements());
    }

    @Transactional
    public void suspendMapping(Long id) {
        UserTenantMapping mapping = userTenantMappingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mapping not found: " + id));
        mapping.suspend();
        userTenantMappingRepository.save(mapping);
        log.info("User-Tenant mapping suspended: {}", id);
    }

    @Transactional
    public void reactivateMapping(Long id) {
        UserTenantMapping mapping = userTenantMappingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mapping not found: " + id));
        mapping.reactivate();
        userTenantMappingRepository.save(mapping);
        log.info("User-Tenant mapping reactivated: {}", id);
    }

    @Transactional
    public void leaveTenant(Long id) {
        UserTenantMapping mapping = userTenantMappingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mapping not found: " + id));
        mapping.leave();
        userTenantMappingRepository.save(mapping);
        log.info("User left tenant: {}", id);
    }

    @Transactional
    public void assignRole(Long userId, Long tenantId, Long roleId, Long assignedBy) {
        if (userRoleMappingRepository.existsByUserIdAndTenantIdAndRoleId(userId, tenantId,
                roleId)) {
            throw new RuntimeException("Role already assigned");
        }
        UserRoleMapping mapping = UserRoleMapping.create(userId, tenantId, roleId, assignedBy);
        userRoleMappingRepository.save(mapping);
        log.info("Role assigned: userId={}, tenantId={}, roleId={}", userId, tenantId, roleId);
    }

    @Transactional
    public void revokeRole(Long userId, Long tenantId, Long roleId) {
        userRoleMappingRepository.deleteByUserIdAndTenantIdAndRoleId(userId, tenantId, roleId);
        log.info("Role revoked: userId={}, tenantId={}, roleId={}", userId, tenantId, roleId);
    }

    public List<UserRoleMappingResponse> getUserRoles(Long userId, Long tenantId) {
        return userRoleMappingRepository.findByUserIdAndTenantId(userId, tenantId).stream()
                .map(this::toRoleResponse).toList();
    }

    private UserTenantMappingResponse toResponse(UserTenantMapping mapping) {
        return UserTenantMappingResponse.builder().id(mapping.getId()).userId(mapping.getUserId())
                .tenantId(mapping.getTenantId()).accountCode(mapping.getAccountCode())
                .employeeNo(mapping.getEmployeeNo())
                .status(mapping.getStatus() != null ? mapping.getStatus().name() : null)
                .preferredLanguage(mapping.getPreferredLanguage()).timezone(mapping.getTimezone())
                .joinedAt(mapping.getJoinedAt()).leftAt(mapping.getLeftAt())
                .createdAt(mapping.getCreatedAt()).updatedAt(mapping.getUpdatedAt()).build();
    }

    private UserRoleMappingResponse toRoleResponse(UserRoleMapping mapping) {
        return UserRoleMappingResponse.builder().id(mapping.getId()).userId(mapping.getUserId())
                .tenantId(mapping.getTenantId()).roleId(mapping.getRoleId())
                .assignedAt(mapping.getAssignedAt()).assignedBy(mapping.getAssignedBy()).build();
    }

    @Builder
    public static class UserTenantMappingResponse {
        public Long id, userId, tenantId;
        public String accountCode, employeeNo, status, preferredLanguage, timezone;
        public java.time.LocalDateTime joinedAt, leftAt, createdAt, updatedAt;
    }

    @Builder
    public static class UserRoleMappingResponse {
        public Long id, userId, tenantId, roleId, assignedBy;
        public java.time.LocalDateTime assignedAt;
    }
}
