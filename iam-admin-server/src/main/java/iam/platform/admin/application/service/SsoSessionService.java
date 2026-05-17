package iam.platform.admin.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import iam.platform.admin.domain.model.entity.User;
import iam.platform.admin.domain.model.entity.Tenant;
import iam.platform.admin.domain.model.entity.TenantAccount;
import iam.platform.admin.domain.repository.UserRepository;
import iam.platform.admin.domain.repository.TenantAccountRepository;
import iam.platform.admin.domain.repository.TenantRepository;
import iam.platform.admin.infrastructure.security.TenantContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing SSO session and tenant switching. Handles tenant selection, switching, and
 * session management for multi-tenant SSO.
 */
@Service
@RequiredArgsConstructor
public class SsoSessionService {

    private final TenantRepository tenantRepository;
    private final TenantAccountRepository tenantAccountRepository;
    private final UserRepository UserRepository;
    private final TenantAccountRoleApplicationService tenantAccountRoleService;

    /**
     * Get all available tenant accounts for the current authenticated user.
     */
    public List<TenantAccountResponse> getAvailableTenants() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        Long userId = extractUserId(auth);
        if (userId == null) {
            throw new IllegalStateException("Cannot determine User ID");
        }

        List<TenantAccount> tenantAccounts = tenantAccountRepository.findByUserId(userId);

        return tenantAccounts.stream().filter(TenantAccount::isActive).map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Switch the current tenant context for the authenticated user.
     */
    public TenantSwitchResponse switchTenant(Long tenantAccountId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        Long userId = extractUserId(auth);
        if (userId == null) {
            throw new IllegalStateException("Cannot determine User ID");
        }

        TenantAccount tenantAccount = tenantAccountRepository.findById(tenantAccountId).orElseThrow(
                () -> new IllegalArgumentException("Tenant account not found: " + tenantAccountId));

        if (!tenantAccount.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Tenant account does not belong to current user");
        }

        if (!tenantAccount.isActive()) {
            throw new IllegalStateException("Tenant account is not active");
        }

        Tenant tenant = tenantRepository.findById(tenantAccount.getTenantId())
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));

        if (!tenant.isActive()) {
            throw new IllegalStateException("Tenant is not active");
        }

        Set<String> permissions =
                tenantAccountRoleService.getTenantAccountPermissions(tenantAccountId).stream()
                        .map(p -> p.getPermissionCode()).collect(Collectors.toSet());

        // Update tenant context via ThreadLocal
        TenantContext.setCurrentUserId(userId);
        TenantContext.setCurrentTenantId(tenant.getId());
        TenantContext.setCurrentTenantAccountId(tenantAccountId);

        return new TenantSwitchResponse(tenant.getId(), tenant.getTenantCode(),
                tenantAccount.getId(), permissions);
    }

    /**
     * Clear the current tenant context.
     */
    public void clearTenantContext() {
        TenantContext.setCurrentTenantId(null);
        TenantContext.setCurrentTenantAccountId(null);
    }

    private Long extractUserId(Authentication auth) {
        // Try to get from OAuth2 session claims
        if (auth instanceof OAuth2AuthenticationToken oauthToken) {
            OAuth2User user = oauthToken.getPrincipal();
            Object userIdAttr = user.getAttribute("user_id");
            if (userIdAttr instanceof Number number) {
                return number.longValue();
            }
        }

        // Try TenantContext
        Long contextUserId = TenantContext.getCurrentUserId();
        if (contextUserId != null) {
            return contextUserId;
        }

        // Fallback: lookup by username
        String username = auth.getName();
        return UserRepository.findByUsername(username).map(User::getId).orElse(null);
    }

    private TenantAccountResponse toResponse(TenantAccount account) {
        return new TenantAccountResponse(account.getId(), account.getTenantId(),
                account.getTenantCode(), account.getEmployeeNo(),
                account.getStatus() != null ? account.getStatus().name() : "UNKNOWN");
    }

    public record TenantAccountResponse(Long tenantAccountId, Long tenantId, String tenantCode,
            String employeeNo, String status) {
    }

    public record TenantSwitchResponse(Long tenantId, String tenantCode, Long tenantAccountId,
            Set<String> permissions) {
    }
}
