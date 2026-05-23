package iam.platform.auth.application.service.listener;

import iam.platform.auth.application.service.TenantAccountRoleApplicationService;
import iam.platform.auth.application.service.event.AuthenticationCompletedEvent;
import iam.platform.auth.domain.model.entity.Tenant;
import iam.platform.auth.domain.model.entity.TenantAccount;
import iam.platform.auth.domain.model.entity.User;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;
import iam.platform.auth.domain.model.valueobject.AuthenticationResult;
import iam.platform.auth.domain.model.valueobject.TenantAwareAuthenticationToken;
import iam.platform.auth.domain.model.valueobject.TenantResolutionResult;
import iam.platform.auth.domain.repository.TenantRepository;
import iam.platform.auth.domain.service.TenantResolutionPolicy;
import iam.platform.common.context.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Listener: handles tenant resolution, permission loading, and security context establishment.
 * Replaces TenantResolutionHandler, PermissionLoadingHandler, and
 * SecurityContextEstablishmentHandler (PostAuthHandlers) to eliminate circular dependencies.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TenantResolutionAndSecurityContextListener {

    private static final String HEADER_TENANT_CODE = "X-Tenant-Code";
    private static final String PARAM_TENANT = "tenant";
    private static final String SESSION_USER_ID = "SESSION_USER_ID";
    private static final String SESSION_TENANT_ID = "SESSION_TENANT_ID";
    private static final String SESSION_TENANT_ACCOUNT_ID = "SESSION_TENANT_ACCOUNT_ID";

    private final TenantResolutionPolicy tenantResolutionPolicy;
    private final TenantAccountRoleApplicationService tenantAccountRoleService;
    private final TenantRepository tenantRepository;

    @Order(2)
    @EventListener
    public void onAuthenticationCompleted(AuthenticationCompletedEvent event) {
        User user = event.getUser();
        AuthenticationMethod method = event.getMethod();
        HttpServletRequest request = event.getRequest();

        log.debug("Processing tenant resolution and security context for user: {}",
                user.getUsername());

        try {
            // Step 1: Resolve tenant
            String tenantCode = extractTenantCode(request);
            TenantResolutionResult resolutionResult =
                    tenantResolutionPolicy.resolve(user, tenantCode);

            TenantAccount selectedAccount = null;
            boolean requiresTenantSelection = false;

            switch (resolutionResult.status()) {
                case AUTO_SELECTED -> {
                    selectedAccount = resolutionResult.selectedAccount();
                    requiresTenantSelection = false;
                }
                case SELECTION_REQUIRED -> {
                    selectedAccount = null;
                    requiresTenantSelection = true;
                }
                case NO_ACCOUNTS -> {
                    selectedAccount = null;
                    requiresTenantSelection = false;
                }
            }

            // Step 2: Load permissions if tenant selected
            Set<String> permissions = Set.of();
            if (!requiresTenantSelection && selectedAccount != null) {
                permissions = tenantAccountRoleService
                        .getTenantAccountPermissions(selectedAccount.getId()).stream()
                        .map(p -> p.getPermissionCode()).collect(Collectors.toSet());
            }

            // Step 3: Write result back to event holder for ProtocolRouter
            AuthenticationResult result;
            if (requiresTenantSelection) {
                result = AuthenticationResult.requiresTenantSelection(user, method,
                        resolutionResult.availableAccounts());
            } else if (selectedAccount != null) {
                result = AuthenticationResult.withSelectedTenant(user, method, selectedAccount,
                        resolutionResult.availableAccounts(), permissions);
            } else {
                result = AuthenticationResult.noTenantAccounts(user, method);
            }
            event.getResultHolder().setResult(result);

            // Step 4: Establish security context if tenant selected
            if (selectedAccount != null && !requiresTenantSelection) {
                establishSecurityContext(user, selectedAccount, permissions, request);
            } else if (requiresTenantSelection) {
                log.debug(
                        "Tenant selection required for user: {}, redirect will be handled by success handler",
                        user.getUsername());
            }
        } catch (Exception e) {
            log.error("Failed to resolve tenant context for user: {}", user.getUsername(), e);
            // Set basic result on failure so the flow can continue
            event.getResultHolder().setResult(AuthenticationResult.basic(user, method));
        }
    }

    private String extractTenantCode(HttpServletRequest request) {
        String tenantCode = request.getHeader(HEADER_TENANT_CODE);
        if (tenantCode != null && !tenantCode.isBlank()) {
            return tenantCode.trim();
        }
        tenantCode = request.getParameter(PARAM_TENANT);
        if (tenantCode != null && !tenantCode.isBlank()) {
            return tenantCode.trim();
        }
        return null;
    }

    private void establishSecurityContext(User user, TenantAccount selectedAccount,
            Set<String> permissions, HttpServletRequest request) {
        Tenant tenant = tenantRepository.findById(selectedAccount.getTenantId())
                .orElseThrow(() -> new IllegalStateException(
                        "Tenant not found: " + selectedAccount.getTenantId()));

        List<SimpleGrantedAuthority> authorities =
                permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

        Authentication wrappedAuth =
                new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);

        TenantAwareAuthenticationToken tenantAuth =
                TenantAwareAuthenticationToken.fromAuthentication(wrappedAuth, tenant.getId(),
                        tenant.getTenantCode(), permissions);

        TenantContext.setCurrentUserId(user.getId());
        TenantContext.setCurrentTenantId(tenant.getId());
        TenantContext.setCurrentTenantAccountId(selectedAccount.getId());

        tenantAuth.registerToContext();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute(SESSION_USER_ID, user.getId());
            session.setAttribute(SESSION_TENANT_ID, tenant.getId());
            session.setAttribute(SESSION_TENANT_ACCOUNT_ID, selectedAccount.getId());
        }

        log.debug("Security context established for user: {}, tenant: {}", user.getUsername(),
                tenant.getTenantCode());
    }
}
