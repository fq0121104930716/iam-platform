package iam.platform.admin.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import iam.platform.common.dto.request.CreateTenantRequest;
import iam.platform.common.dto.request.UpdateTenantRequest;
import iam.platform.common.dto.response.TenantResponse;
import iam.platform.common.model.annotation.AuditLog;
import iam.platform.admin.domain.model.entity.Tenant;
import iam.platform.common.model.enums.AuditEventType;
import iam.platform.common.model.exception.ConflictException;
import iam.platform.common.model.exception.TenantNotFoundException;
import iam.platform.admin.domain.repository.TenantAccountRepository;
import iam.platform.admin.domain.repository.TenantRepository;
import iam.platform.common.api.PageResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantApplicationService {

    private final TenantRepository tenantRepository;
    private final TenantAccountRepository tenantAccountRepository;

    @Transactional
    @AuditLog(value = AuditEventType.TENANT_CREATED, resourceType = "tenant", action = "创建租户 #{#request.tenantName}")
    public TenantResponse createTenant(CreateTenantRequest request) {
        if (tenantRepository.existsByTenantCode(request.getTenantCode())) {
            throw new ConflictException("Tenant code already exists: " + request.getTenantCode());
        }

        // Domain factory method handles construction with defaults
        Tenant tenant = Tenant.create(
                request.getTenantCode(),
                request.getTenantName(),
                request.getMaxUsers(),
                request.getExpiresAt(),
                request.getContactEmail(),
                request.getContactPhone());

        tenant = tenantRepository.save(tenant);
        log.info("Tenant created: {} (code: {})", tenant.getTenantName(), tenant.getTenantCode());
        return toResponse(tenant);
    }

    public TenantResponse getTenant(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + id));
        return toResponse(tenant);
    }

    @Transactional
    @AuditLog(value = AuditEventType.TENANT_UPDATED, resourceType = "tenant", action = "更新租户 ID=#{#id}")
    public TenantResponse updateTenant(Long id, UpdateTenantRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + id));

        // Delegate to domain behavior method
        tenant.updateInfo(
                request.getTenantName(),
                request.getMaxUsers(),
                request.getContactEmail(),
                request.getContactPhone(),
                request.getExpiresAt());

        tenant = tenantRepository.save(tenant);
        log.info("Tenant updated: {}", tenant.getTenantCode());
        return toResponse(tenant);
    }

    @Transactional
    @AuditLog(value = AuditEventType.TENANT_DELETED, resourceType = "tenant", action = "删除租户 ID=#{#id}")
    public void deleteTenant(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + id));

        // Business rule enforced by domain entity
        tenant.markDeleted();
        tenantRepository.save(tenant);
        log.info("Tenant deleted: {}", id);
    }

    @Transactional
    @AuditLog(value = AuditEventType.TENANT_ACTIVATED, resourceType = "tenant", action = "激活租户 ID=#{#id}")
    public void activateTenant(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + id));

        // State machine logic in domain entity
        tenant.activate();
        tenantRepository.save(tenant);
        log.info("Tenant activated: {}", id);
    }

    @Transactional
    @AuditLog(value = AuditEventType.TENANT_SUSPENDED, resourceType = "tenant", action = "暂停租户 ID=#{#id}")
    public void suspendTenant(Long id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found: " + id));

        // State machine logic in domain entity
        tenant.suspend();
        tenantRepository.save(tenant);
        log.info("Tenant suspended: {}", id);
    }

    public PageResponse<TenantResponse> listTenants(int page, int size) {
        Page<Tenant> tenantPage = tenantRepository.findAll(PageRequest.of(page, size));
        return PageResponse.of(tenantPage.getContent().stream().map(this::toResponse).toList(),
                tenantPage.getNumber(), tenantPage.getSize(), tenantPage.getTotalElements());
    }

    private TenantResponse toResponse(Tenant tenant) {
        long userCount = tenantAccountRepository.countByTenantIdAndStatus(tenant.getId(), "ACTIVE")
                + tenantAccountRepository.countByTenantIdAndStatus(tenant.getId(), "SUSPENDED");

        return TenantResponse.builder().id(tenant.getId()).tenantCode(tenant.getTenantCode())
                .tenantName(tenant.getTenantName())
                .status(tenant.getStatus() != null ? tenant.getStatus().name() : null)
                .maxUsers(tenant.getMaxUsers()).currentUsers((int) userCount)
                .contactEmail(tenant.getContactEmail()).contactPhone(tenant.getContactPhone())
                .expiresAt(tenant.getExpiresAt()).createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt()).build();
    }
}
