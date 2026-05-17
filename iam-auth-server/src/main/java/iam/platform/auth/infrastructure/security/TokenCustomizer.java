package iam.platform.auth.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;
import iam.platform.auth.application.service.TenantAccountRoleApplicationService;
import iam.platform.auth.domain.model.entity.Person;
import iam.platform.auth.domain.model.entity.TenantAccount;
import iam.platform.auth.domain.repository.PersonRepository;
import iam.platform.auth.domain.repository.TenantAccountRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Customizes JWT tokens to include multi-tenant context and permissions.
 *
 * Added claims: - tenant_id: Current tenant ID (if tenant context is established) - tenant_code:
 * Current tenant code - tenant_account_id: Current tenant account ID - roles: List of role codes
 * for the current tenant - permissions: List of permission codes for the current tenant - email:
 * User email - nickname: User nickname
 */
@Component
@RequiredArgsConstructor
public class TokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    private final PersonRepository personRepository;
    private final TenantAccountRepository tenantAccountRepository;
    private final TenantAccountRoleApplicationService tenantAccountRoleService;

    @Override
    public void customize(JwtEncodingContext context) {
        String username = context.getPrincipal().getName();

        // Find person
        Person person = personRepository.findByUsername(username).orElse(null);
        if (person == null) {
            return;
        }

        // Add basic user claims
        context.getClaims().claim("email", person.getEmail());
        if (person.getNickname() != null) {
            context.getClaims().claim("nickname", person.getNickname());
        }
        context.getClaims().claim("person_id", person.getId());

        // Try to get tenant context from current request
        Long tenantId = TenantContext.getCurrentTenantId();
        Long tenantAccountId = TenantContext.getCurrentTenantAccountId();

        if (tenantId != null && tenantAccountId != null) {
            // Tenant context is established - add tenant-specific claims
            addTenantClaims(context, tenantId, tenantAccountId, person);
        } else {
            // No tenant context - add all tenant accounts for client to choose
            addAllTenantAccountsClaims(context, person);
        }
    }

    /**
     * Add claims for a specific tenant context.
     */
    private void addTenantClaims(JwtEncodingContext context, Long tenantId, Long tenantAccountId,
            Person person) {
        // Add tenant ID and account ID
        context.getClaims().claim("tenant_id", tenantId);
        context.getClaims().claim("tenant_account_id", tenantAccountId);

        // Get tenant account details
        TenantAccount tenantAccount =
                tenantAccountRepository.findById(tenantAccountId).orElse(null);
        if (tenantAccount != null) {
            context.getClaims().claim("tenant_code", tenantAccount.getTenantCode());
            context.getClaims().claim("employee_no", tenantAccount.getEmployeeNo());

            // Add roles for this tenant account
            if (tenantAccount.getRoles() != null) {
                List<String> roles = tenantAccount.getRoles().stream().map(role -> role.getCode())
                        .collect(Collectors.toList());
                context.getClaims().claim("roles", roles);
            }

            // Add permissions for this tenant account
            try {
                Set<String> permissions = tenantAccountRoleService
                        .getTenantAccountPermissions(tenantAccountId).stream()
                        .map(p -> p.getPermissionCode()).collect(Collectors.toSet());
                context.getClaims().claim("permissions", permissions);
            } catch (Exception e) {
                // If permission loading fails, don't block token generation
                context.getClaims().claim("permissions", List.of());
            }
        }
    }

    /**
     * Add claims for all tenant accounts (when no specific tenant is selected). This allows the
     * client application to prompt user to select a tenant.
     */
    private void addAllTenantAccountsClaims(JwtEncodingContext context, Person person) {
        List<TenantAccount> tenantAccounts = tenantAccountRepository.findByPersonId(person.getId());

        if (!tenantAccounts.isEmpty()) {
            // Add list of available tenant accounts
            List<java.util.Map<String, Object>> tenantAccountList =
                    tenantAccounts.stream().filter(TenantAccount::isActive).map(account -> {
                        java.util.Map<String, Object> accountInfo = new java.util.HashMap<>();
                        accountInfo.put("tenant_account_id", account.getId());
                        accountInfo.put("tenant_id", account.getTenantId());
                        accountInfo.put("tenant_code", account.getTenantCode());
                        accountInfo.put("employee_no", account.getEmployeeNo());
                        return accountInfo;
                    }).collect(Collectors.toList());

            context.getClaims().claim("tenant_accounts", tenantAccountList);
        }

        // Add empty tenant context indicators
        context.getClaims().claim("tenant_id", null);
        context.getClaims().claim("roles", List.of());
        context.getClaims().claim("permissions", List.of());
    }
}
