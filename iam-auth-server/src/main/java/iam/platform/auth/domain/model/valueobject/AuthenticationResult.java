package iam.platform.auth.domain.model.valueobject;

import iam.platform.auth.domain.model.entity.User;
import iam.platform.auth.domain.model.entity.TenantAccount;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Immutable value object representing the outcome of the authentication pipeline.
 */
public record AuthenticationResult(User user, AuthenticationMethod method,
        TenantAccount selectedTenantAccount, List<TenantAccount> availableTenantAccounts,
        Set<String> permissions, boolean requiresTenantSelection, LocalDateTime authenticatedAt) {
    public AuthenticationResult {
        if (user == null)
            throw new IllegalArgumentException("User is required");
        if (method == null)
            throw new IllegalArgumentException("Authentication method is required");
        if (availableTenantAccounts == null)
            throw new IllegalArgumentException("Available tenant accounts list is required");
        if (authenticatedAt == null)
            authenticatedAt = LocalDateTime.now();
    }

    /**
     * Create a result indicating tenant selection is required.
     */
    public static AuthenticationResult requiresTenantSelection(User user,
            AuthenticationMethod method, List<TenantAccount> availableAccounts) {
        return new AuthenticationResult(user, method, null, availableAccounts, Set.of(), true,
                LocalDateTime.now());
    }

    /**
     * Create a result with an auto-selected tenant account.
     */
    public static AuthenticationResult withSelectedTenant(User user,
            AuthenticationMethod method, TenantAccount selectedAccount,
            List<TenantAccount> availableAccounts, Set<String> permissions) {
        return new AuthenticationResult(user, method, selectedAccount, availableAccounts,
                permissions, false, LocalDateTime.now());
    }

    /**
     * Create a result with no tenant accounts (user needs onboarding).
     */
    public static AuthenticationResult noTenantAccounts(User user,
            AuthenticationMethod method) {
        return new AuthenticationResult(user, method, null, List.of(), Set.of(), false,
                LocalDateTime.now());
    }
}
