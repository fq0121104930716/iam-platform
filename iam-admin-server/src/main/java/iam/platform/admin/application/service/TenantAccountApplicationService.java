package iam.platform.admin.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import iam.platform.common.dto.request.CreateTenantAccountRequest;
import iam.platform.common.dto.request.UpdateTenantAccountRequest;
import iam.platform.common.dto.response.TenantAccountResponse;
import iam.platform.admin.domain.model.entity.Tenant;
import iam.platform.admin.domain.model.entity.TenantAccount;
import iam.platform.common.model.exception.ConflictException;
import iam.platform.common.model.exception.TenantAccountNotFoundException;
import iam.platform.common.model.exception.TenantNotFoundException;
import iam.platform.common.model.exception.PersonNotFoundException;
import iam.platform.admin.domain.repository.PersonRepository;
import iam.platform.admin.domain.repository.TenantAccountRepository;
import iam.platform.admin.domain.repository.TenantRepository;
import iam.platform.admin.domain.service.TenantAccountCreationPolicy;
import iam.platform.common.api.PageResponse;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantAccountApplicationService {

        private final TenantAccountRepository tenantAccountRepository;
        private final PersonRepository personRepository;
        private final TenantRepository tenantRepository;
        private final TenantAccountCreationPolicy tenantAccountCreationPolicy;

        @Transactional
        public TenantAccountResponse createTenantAccount(Long personId,
                        CreateTenantAccountRequest request) {
                if (!personRepository.findById(personId).isPresent()) {
                        throw new PersonNotFoundException("Person not found: " + personId);
                }

                // Validate preconditions via domain service
                tenantAccountCreationPolicy.validateCreationPreconditions(request.getTenantId(),
                                request.getAccountCode(), request.getEmployeeNo());

                // Create via domain factory method
                TenantAccount tenantAccount = TenantAccount.create(personId, request.getTenantId(),
                                request.getAccountCode(), request.getEmployeeNo());

                // Apply optional preferences if provided
                if (request.getPreferredLanguage() != null || request.getTimezone() != null) {
                        tenantAccount.updatePreferences(request.getPreferredLanguage(),
                                        request.getTimezone());
                }

                tenantAccount = tenantAccountRepository.save(tenantAccount);
                log.info("Tenant account created: personId={}, tenantId={}, accountCode={}",
                                personId, request.getTenantId(), request.getAccountCode());

                Tenant tenant = tenantRepository.findById(request.getTenantId()).orElse(null);
                return toResponse(tenantAccount, tenant);
        }

        public TenantAccountResponse getTenantAccount(Long id) {
                TenantAccount tenantAccount = tenantAccountRepository.findById(id)
                                .orElseThrow(() -> new TenantAccountNotFoundException(
                                                "Tenant account not found: " + id));

                Long tId = tenantAccount.getTenantId();
                Tenant tenant = tenantRepository.findById(tId).orElseThrow(
                                () -> new TenantNotFoundException("Tenant not found: " + tId));

                return toResponse(tenantAccount, tenant);
        }

        @Transactional
        public TenantAccountResponse updateTenantAccount(Long id,
                        UpdateTenantAccountRequest request) {
                TenantAccount tenantAccount = tenantAccountRepository.findById(id)
                                .orElseThrow(() -> new TenantAccountNotFoundException(
                                                "Tenant account not found: " + id));

                // Employee number uniqueness check (cross-aggregate, stays in app service)
                if (request.getEmployeeNo() != null
                                && !request.getEmployeeNo().equals(tenantAccount.getEmployeeNo())
                                && tenantAccountRepository.existsByTenantIdAndEmployeeNo(
                                                tenantAccount.getTenantId(),
                                                request.getEmployeeNo())) {
                        throw new ConflictException("Employee number already exists: "
                                        + request.getEmployeeNo());
                }

                // Delegate to domain methods
                if (request.getEmployeeNo() != null) {
                        tenantAccount.updateEmployeeNo(request.getEmployeeNo());
                }
                tenantAccount.updatePreferences(request.getPreferredLanguage(),
                                request.getTimezone());

                tenantAccount = tenantAccountRepository.save(tenantAccount);
                log.info("Tenant account updated: {}", id);

                Long savedTenantId = tenantAccount.getTenantId();
                Tenant tenant = tenantRepository.findById(savedTenantId)
                                .orElseThrow(() -> new TenantNotFoundException(
                                                "Tenant not found: " + savedTenantId));

                return toResponse(tenantAccount, tenant);
        }

        @Transactional
        public void suspendTenantAccount(Long id) {
                TenantAccount tenantAccount = tenantAccountRepository.findById(id)
                                .orElseThrow(() -> new TenantAccountNotFoundException(
                                                "Tenant account not found: " + id));

                tenantAccount.suspend();
                tenantAccountRepository.save(tenantAccount);
                log.info("Tenant account suspended: {}", id);
        }

        @Transactional
        public void reactivateTenantAccount(Long id) {
                TenantAccount tenantAccount = tenantAccountRepository.findById(id)
                                .orElseThrow(() -> new TenantAccountNotFoundException(
                                                "Tenant account not found: " + id));

                tenantAccount.reactivate();
                tenantAccountRepository.save(tenantAccount);
                log.info("Tenant account reactivated: {}", id);
        }

        @Transactional
        public void leaveTenant(Long id) {
                TenantAccount tenantAccount = tenantAccountRepository.findById(id)
                                .orElseThrow(() -> new TenantAccountNotFoundException(
                                                "Tenant account not found: " + id));

                tenantAccount.leave();
                tenantAccountRepository.save(tenantAccount);
                log.info("Tenant account left: {}", id);
        }

        public List<TenantAccountResponse> getTenantAccountsByPersonId(Long personId) {
                List<TenantAccount> tenantAccounts = personRepository.findById(personId)
                                .map(p -> tenantAccountRepository.findByPersonId(p.getId()))
                                .orElse(List.of());

                return tenantAccounts.stream().map(ta -> {
                        Tenant tenant = tenantRepository.findById(ta.getTenantId()).orElse(null);
                        return toResponse(ta, tenant);
                }).toList();
        }

        public PageResponse<TenantAccountResponse> listTenantAccounts(Long tenantId, int page,
                        int size) {
                Page<TenantAccount> tenantAccountPage = tenantAccountRepository
                                .findByTenantId(tenantId, PageRequest.of(page, size));
                Tenant tenant = tenantRepository.findById(tenantId).orElse(null);

                return PageResponse.of(
                                tenantAccountPage.getContent().stream()
                                                .map(ta -> toResponse(ta, tenant)).toList(),
                                tenantAccountPage.getNumber(), tenantAccountPage.getSize(),
                                tenantAccountPage.getTotalElements());
        }

        private TenantAccountResponse toResponse(TenantAccount tenantAccount, Tenant tenant) {
                return TenantAccountResponse.builder().id(tenantAccount.getId())
                                .personId(tenantAccount.getPersonId())
                                .tenantId(tenantAccount.getTenantId())
                                .tenantCode(tenant != null ? tenant.getTenantCode() : null)
                                .tenantName(tenant != null ? tenant.getTenantName() : null)
                                .accountCode(tenantAccount.getAccountCode())
                                .employeeNo(tenantAccount.getEmployeeNo())
                                .status(tenantAccount.getStatus() != null
                                                ? tenantAccount.getStatus().name()
                                                : null)
                                .joinedAt(tenantAccount.getJoinedAt())
                                .leftAt(tenantAccount.getLeftAt())
                                .preferredLanguage(tenantAccount.getPreferredLanguage())
                                .timezone(tenantAccount.getTimezone())
                                .createdAt(tenantAccount.getCreatedAt())
                                .updatedAt(tenantAccount.getUpdatedAt()).build();
        }
}
