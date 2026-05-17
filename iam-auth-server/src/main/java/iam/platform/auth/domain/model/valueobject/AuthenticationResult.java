package iam.platform.auth.domain.model.valueobject;

import iam.platform.auth.domain.model.entity.Person;
import iam.platform.auth.domain.model.entity.TenantAccount;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Immutable value object representing the outcome of the authentication pipeline.
 */
public record AuthenticationResult(Person person, AuthenticationMethod method,
        TenantAccount selectedTenantAccount, List<TenantAccount> availableTenantAccounts,
        Set<String> permissions, boolean requiresTenantSelection, LocalDateTime authenticatedAt) {
    public AuthenticationResult {
        if (person == null)
            throw new IllegalArgumentException("Person is required");
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
    public static AuthenticationResult requiresTenantSelection(Person person,
            AuthenticationMethod method, List<TenantAccount> availableAccounts) {
        return new AuthenticationResult(person, method, null, availableAccounts, Set.of(), true,
                LocalDateTime.now());
    }

    /**
     * Create a result with an auto-selected tenant account.
     */
    public static AuthenticationResult withSelectedTenant(Person person,
            AuthenticationMethod method, TenantAccount selectedAccount,
            List<TenantAccount> availableAccounts, Set<String> permissions) {
        return new AuthenticationResult(person, method, selectedAccount, availableAccounts,
                permissions, false, LocalDateTime.now());
    }

    /**
     * Create a result with no tenant accounts (user needs onboarding).
     */
    public static AuthenticationResult noTenantAccounts(Person person,
            AuthenticationMethod method) {
        return new AuthenticationResult(person, method, null, List.of(), Set.of(), false,
                LocalDateTime.now());
    }
}
