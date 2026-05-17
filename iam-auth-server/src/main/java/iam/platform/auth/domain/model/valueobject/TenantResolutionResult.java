package iam.platform.auth.domain.model.valueobject;

import iam.platform.auth.domain.model.entity.TenantAccount;

import java.util.List;

/**
 * Result of tenant resolution policy evaluation.
 */
public record TenantResolutionResult(TenantAccount selectedAccount,
        List<TenantAccount> availableAccounts, ResolutionStatus status) {
    public enum ResolutionStatus {
        /** Exactly one tenant account or explicitly requested - auto-selected */
        AUTO_SELECTED,
        /** Multiple tenant accounts available - UI selection required */
        SELECTION_REQUIRED,
        /** No tenant accounts found - user needs onboarding */
        NO_ACCOUNTS
    }

    public static TenantResolutionResult autoSelected(TenantAccount account,
            List<TenantAccount> allAccounts) {
        return new TenantResolutionResult(account, allAccounts, ResolutionStatus.AUTO_SELECTED);
    }

    public static TenantResolutionResult selectionRequired(List<TenantAccount> allAccounts) {
        return new TenantResolutionResult(null, allAccounts, ResolutionStatus.SELECTION_REQUIRED);
    }

    public static TenantResolutionResult noAccounts() {
        return new TenantResolutionResult(null, List.of(), ResolutionStatus.NO_ACCOUNTS);
    }
}
