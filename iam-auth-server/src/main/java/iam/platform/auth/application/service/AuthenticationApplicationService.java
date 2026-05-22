package iam.platform.auth.application.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import iam.platform.auth.application.service.pipeline.PostAuthenticationPipeline;
import iam.platform.auth.domain.model.entity.User;
import iam.platform.auth.domain.model.entity.Tenant;
import iam.platform.auth.domain.model.entity.TenantAccount;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;
import iam.platform.auth.domain.model.valueobject.AuthenticationResult;
import iam.platform.auth.domain.repository.UserRepository;
import iam.platform.auth.domain.repository.TenantAccountRepository;
import iam.platform.auth.domain.repository.TenantRepository;
import iam.platform.auth.domain.model.valueobject.TenantAwareAuthenticationToken;
import iam.platform.common.context.TenantContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Unified application service for authentication completion and tenant management. Replaces
 * VerificationCodeApplicationService and SsoSessionService authentication logic.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationApplicationService {

        private final PostAuthenticationPipeline pipeline;
        private final TenantAccountRepository tenantAccountRepository;
        private final TenantRepository tenantRepository;
        private final UserRepository userRepository;
        private final TenantAccountRoleApplicationService tenantAccountRoleService;

        /**
         * Complete authentication by running the post-authentication pipeline. Called by
         * UnifiedAuthenticationSuccessHandler for both first-party and OAuth2 auth.
         */
        public AuthenticationResult completeAuthentication(User user, AuthenticationMethod method,
                        HttpServletRequest request) {
                return pipeline.execute(user, method, request);
        }

        /**
         * Select a specific tenant account and establish full authentication context. Called by
         * TenantSelectionController after user picks a tenant.
         */
        public AuthenticationResult selectTenant(Long tenantAccountId) {
                // Get current User from TenantContext or SecurityContext
                Long userId = TenantContext.getCurrentUserId();
                if (userId == null) {
                        throw new IllegalStateException(
                                        "Cannot determine User ID - not authenticated");
                }

                User user = userRepository.findById(userId).orElseThrow(
                                () -> new IllegalStateException("User not found: " + userId));

                TenantAccount tenantAccount = tenantAccountRepository.findById(tenantAccountId)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Tenant account not found: " + tenantAccountId));

                if (!tenantAccount.getUserId().equals(userId)) {
                        throw new IllegalArgumentException(
                                        "Tenant account does not belong to current user");
                }
                if (!tenantAccount.isActive()) {
                        throw new IllegalStateException("Tenant account is not active");
                }

                Tenant tenant = tenantRepository.findById(tenantAccount.getTenantId()).orElseThrow(
                                () -> new IllegalArgumentException("Tenant not found"));
                if (!tenant.isActive()) {
                        throw new IllegalStateException("Tenant is not active");
                }

                // Load permissions
                Set<String> permissions = tenantAccountRoleService
                                .getTenantAccountPermissions(tenantAccountId).stream()
                                .map(p -> p.getPermissionCode()).collect(Collectors.toSet());

                // Build tenant-aware authentication token
                Authentication wrappedAuth =
                                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                                user.getUsername(), null,
                                                permissions.stream()
                                                                .map(SimpleGrantedAuthority::new)
                                                                .collect(Collectors.toList()));

                TenantAwareAuthenticationToken tenantAuth = TenantAwareAuthenticationToken
                                .fromAuthentication(wrappedAuth, tenant.getId(),
                                                tenant.getTenantCode(), permissions);

                // Update TenantContext
                TenantContext.setCurrentUserId(userId);
                TenantContext.setCurrentTenantId(tenant.getId());
                TenantContext.setCurrentTenantAccountId(tenantAccountId);
                tenantAuth.registerToContext();

                // Get all active tenant accounts for the result
                List<TenantAccount> allActiveAccounts = tenantAccountRepository.findByUserId(userId)
                                .stream().filter(TenantAccount::isActive)
                                .collect(Collectors.toList());

                return AuthenticationResult.withSelectedTenant(user, AuthenticationMethod.PASSWORD,
                                tenantAccount, allActiveAccounts, permissions);
        }

        /**
         * Get all available tenant accounts for the current authenticated user. Query-only
         * operation, kept from SsoSessionService.
         */
        public List<TenantAccountResponse> getAvailableTenants() {
                Long userId = TenantContext.getCurrentUserId();
                if (userId == null) {
                        throw new IllegalStateException("User not authenticated");
                }

                List<TenantAccount> tenantAccounts = tenantAccountRepository.findByUserId(userId);

                return tenantAccounts.stream().filter(TenantAccount::isActive).map(this::toResponse)
                                .collect(Collectors.toList());
        }

        private TenantAccountResponse toResponse(TenantAccount account) {
                return new TenantAccountResponse(account.getId(), account.getTenantId(),
                                account.getTenantCode(), account.getEmployeeNo(),
                                account.getStatus() != null ? account.getStatus().name()
                                                : "UNKNOWN");
        }

        public record TenantAccountResponse(Long tenantAccountId, Long tenantId, String tenantCode,
                        String employeeNo, String status) {
        }
}
