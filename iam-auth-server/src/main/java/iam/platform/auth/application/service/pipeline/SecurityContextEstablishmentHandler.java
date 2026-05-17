package iam.platform.auth.application.service.pipeline;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import iam.platform.auth.domain.model.entity.User;
import iam.platform.auth.domain.model.entity.Tenant;
import iam.platform.auth.domain.model.entity.TenantAccount;
import iam.platform.auth.domain.repository.TenantRepository;
import iam.platform.auth.infrastructure.security.TenantAwareAuthenticationToken;
import iam.platform.auth.infrastructure.security.TenantContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pipeline handler: builds the TenantAwareAuthenticationToken and establishes it in the
 * SecurityContext and TenantContext for the current session.
 */
@Component
@RequiredArgsConstructor
public class SecurityContextEstablishmentHandler implements PostAuthHandler {

    private static final String SESSION_TENANT_ID = "SESSION_TENANT_ID";
    private static final String SESSION_TENANT_ACCOUNT_ID = "SESSION_TENANT_ACCOUNT_ID";

    private final TenantRepository tenantRepository;

    @Override
    public void handle(PostAuthContext context) {
        User user = context.getUser();
        TenantAccount selectedAccount = context.getSelectedTenantAccount();

        if (selectedAccount == null) {
            // No tenant selected - skip establishing full authentication context
            // Tenant selection is required, user will be redirected to select-tenant page
            context.setResultAuthentication(null);
            return;
        }

        // Get tenant details
        Tenant tenant = tenantRepository.findById(selectedAccount.getTenantId())
                .orElseThrow(() -> new IllegalStateException(
                        "Tenant not found: " + selectedAccount.getTenantId()));

        // Build authorities from permissions
        Set<String> permissions = context.getPermissions();
        List<SimpleGrantedAuthority> authorities =
                permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

        // Build basic auth as the wrapped authentication
        Authentication wrappedAuth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        user.getUsername(), null, authorities);

        // Create tenant-aware token
        TenantAwareAuthenticationToken tenantAuth =
                TenantAwareAuthenticationToken.fromAuthentication(wrappedAuth, tenant.getId(),
                        tenant.getTenantCode(), permissions);

        // Set TenantContext ThreadLocal
        TenantContext.setCurrentUserId(user.getId());
        TenantContext.setCurrentTenantId(tenant.getId());
        TenantContext.setCurrentTenantAccountId(selectedAccount.getId());

        // Register to SecurityContext
        tenantAuth.registerToContext();
        context.setResultAuthentication(tenantAuth);

        // Store tenant IDs in session for subsequent requests
        HttpSession session = context.getRequest().getSession(false);
        if (session != null) {
            session.setAttribute(SESSION_TENANT_ID, tenant.getId());
            session.setAttribute(SESSION_TENANT_ACCOUNT_ID, selectedAccount.getId());
        }
    }

    @Override
    public int getOrder() {
        return 500;
    }
}
