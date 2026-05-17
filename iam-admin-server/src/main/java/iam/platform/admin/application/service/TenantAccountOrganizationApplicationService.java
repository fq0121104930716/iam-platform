package iam.platform.admin.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import iam.platform.common.dto.response.OrganizationResponse;
import iam.platform.common.dto.response.TenantAccountResponse;
import iam.platform.admin.domain.model.entity.Organization;
import iam.platform.admin.domain.model.entity.TenantAccount;
import iam.platform.admin.domain.model.entity.TenantAccountOrganizationMapping;
import iam.platform.common.model.enums.OrgStatus;
import iam.platform.common.model.exception.ConflictException;
import iam.platform.common.model.exception.OrganizationNotFoundException;
import iam.platform.admin.domain.repository.OrganizationRepository;
import iam.platform.admin.domain.repository.TenantAccountOrganizationMappingRepository;
import iam.platform.admin.domain.repository.TenantAccountRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantAccountOrganizationApplicationService {

    private final TenantAccountRepository tenantAccountRepository;
    private final TenantAccountOrganizationMappingRepository organizationMappingRepository;
    private final OrganizationRepository organizationRepository;

    /**
     * 将租户账号添加到组织
     */
    @Transactional
    public void assignTenantAccountToOrganization(Long tenantAccountId, Long organizationId,
            Boolean isPrimary, String position) {
        // 验证租户账号存在
        tenantAccountRepository.findById(tenantAccountId).orElseThrow(
                () -> new IllegalArgumentException("Tenant account not found: " + tenantAccountId));

        // 验证组织存在
        organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException(
                        "Organization not found: " + organizationId));

        // 检查是否已关联
        if (organizationMappingRepository.existsByTenantAccountIdAndOrganizationId(tenantAccountId,
                organizationId)) {
            throw new ConflictException("Tenant account already belongs to this organization");
        }

        // 如果是主组织，检查是否已有其他主组织
        if (Boolean.TRUE.equals(isPrimary)) {
            List<TenantAccountOrganizationMapping> existingMappings =
                    organizationMappingRepository.findByTenantAccountId(tenantAccountId);
            boolean hasPrimary =
                    existingMappings.stream().anyMatch(m -> Boolean.TRUE.equals(m.getIsPrimary()));
            if (hasPrimary) {
                throw new ConflictException("Tenant account already has a primary organization");
            }
        }

        // 创建组织关联
        TenantAccountOrganizationMapping mapping = TenantAccountOrganizationMapping.builder()
                .tenantAccountId(tenantAccountId).organizationId(organizationId)
                .isPrimary(isPrimary != null ? isPrimary : false).position(position)
                .joinedOrgAt(LocalDateTime.now()).build();
        organizationMappingRepository.save(mapping);

        log.info("Tenant account {} assigned to organization {}", tenantAccountId, organizationId);
    }

    /**
     * 从组织移除租户账号
     */
    @Transactional
    public void removeTenantAccountFromOrganization(Long tenantAccountId, Long organizationId) {
        if (!organizationMappingRepository.existsByTenantAccountIdAndOrganizationId(tenantAccountId,
                organizationId)) {
            throw new IllegalArgumentException(
                    "Tenant account does not belong to this organization");
        }
        organizationMappingRepository.deleteByTenantAccountIdAndOrganizationId(tenantAccountId,
                organizationId);
        log.info("Tenant account {} removed from organization {}", tenantAccountId, organizationId);
    }

    /**
     * 获取租户账号所属的组织列表
     */
    public List<OrganizationResponse> getTenantAccountOrganizations(Long tenantAccountId) {
        List<TenantAccountOrganizationMapping> mappings =
                organizationMappingRepository.findByTenantAccountId(tenantAccountId);

        return mappings.stream()
                .map(mapping -> organizationRepository.findById(mapping.getOrganizationId()))
                .filter(opt -> opt.isPresent())
                .map(opt -> toOrganizationResponse(opt.get(),
                        mappings.stream()
                                .filter(m -> m.getOrganizationId().equals(opt.get().getId()))
                                .findFirst().orElse(null)))
                .collect(Collectors.toList());
    }

    /**
     * 获取组织的成员列表
     */
    public List<TenantAccountResponse> getOrganizationMembers(Long organizationId) {
        List<TenantAccountOrganizationMapping> mappings =
                organizationMappingRepository.findByOrganizationId(organizationId);

        return mappings.stream()
                .map(mapping -> tenantAccountRepository.findById(mapping.getTenantAccountId()))
                .filter(opt -> opt.isPresent()).map(opt -> toTenantAccountResponse(opt.get()))
                .collect(Collectors.toList());
    }

    /**
     * 设置主组织
     */
    @Transactional
    public void setPrimaryOrganization(Long tenantAccountId, Long organizationId) {
        // 验证租户账号是否属于该组织
        TenantAccountOrganizationMapping mapping = organizationMappingRepository
                .findByTenantAccountIdAndOrganizationId(tenantAccountId, organizationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tenant account does not belong to this organization"));

        // 取消其他主组织
        List<TenantAccountOrganizationMapping> existingMappings =
                organizationMappingRepository.findByTenantAccountId(tenantAccountId);
        for (TenantAccountOrganizationMapping existing : existingMappings) {
            if (Boolean.TRUE.equals(existing.getIsPrimary())
                    && !existing.getId().equals(mapping.getId())) {
                existing.setIsPrimary(false);
                organizationMappingRepository.save(existing);
            }
        }

        // 设置新的主组织
        mapping.setIsPrimary(true);
        organizationMappingRepository.save(mapping);

        log.info("Primary organization set to {} for tenant account {}", organizationId,
                tenantAccountId);
    }

    private OrganizationResponse toOrganizationResponse(Organization org,
            TenantAccountOrganizationMapping mapping) {
        boolean isPrimary = mapping != null && Boolean.TRUE.equals(mapping.getIsPrimary());
        String position = mapping != null ? mapping.getPosition() : null;

        return OrganizationResponse.builder().id(org.getId()).tenantId(org.getTenantId())
                .orgCode(org.getOrgCode()).orgName(org.getOrgName()).orgType(org.getOrgType())
                .parentId(org.getParentId()).level(org.getLevel()).path(org.getPath())
                .sortOrder(org.getSortOrder()).managerId(org.getManagerId()).phone(org.getPhone())
                .email(org.getEmail())
                .status(org.getStatus() != null ? org.getStatus().name() : OrgStatus.ACTIVE.name())
                .description(org.getDescription()).children(new ArrayList<>()).memberCount(0)
                .isPrimary(isPrimary).position(position).createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt()).build();
    }

    private TenantAccountResponse toTenantAccountResponse(TenantAccount account) {
        return TenantAccountResponse.builder().id(account.getId()).personId(account.getPersonId())
                .tenantId(account.getTenantId()).accountCode(account.getAccountCode())
                .employeeNo(account.getEmployeeNo())
                .status(account.getStatus() != null ? account.getStatus().name() : null)
                .joinedAt(account.getJoinedAt()).leftAt(account.getLeftAt())
                .preferredLanguage(account.getPreferredLanguage()).timezone(account.getTimezone())
                .createdAt(account.getCreatedAt()).updatedAt(account.getUpdatedAt()).build();
    }
}
