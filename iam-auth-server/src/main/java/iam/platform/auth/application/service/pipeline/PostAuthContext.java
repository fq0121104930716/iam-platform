package iam.platform.auth.application.service.pipeline;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import iam.platform.auth.domain.model.entity.User;
import iam.platform.auth.domain.model.entity.TenantAccount;
import iam.platform.auth.domain.model.enums.AuthenticationMethod;
import iam.platform.auth.domain.model.valueobject.AuthenticationResult;
import iam.platform.auth.domain.model.valueobject.TenantAwareAuthenticationToken;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Mutable context object passed through the post-authentication pipeline. Each handler mutates the
 * context to build the final AuthenticationResult.
 */
@Getter
public class PostAuthContext {
    private final User user;
    private final AuthenticationMethod method;
    private final HttpServletRequest request;
    private String requestedTenantCode;
    private TenantAccount selectedTenantAccount;
    private List<TenantAccount> availableTenantAccounts;
    private Set<String> permissions;
    private boolean requiresTenantSelection;
    private TenantAwareAuthenticationToken resultAuthentication;

    public PostAuthContext(User user, AuthenticationMethod method, HttpServletRequest request) {
        this.user = user;
        this.method = method;
        this.request = request;
    }

    public void setRequestedTenantCode(String requestedTenantCode) {
        this.requestedTenantCode = requestedTenantCode;
    }

    public void setSelectedTenantAccount(TenantAccount selectedTenantAccount) {
        this.selectedTenantAccount = selectedTenantAccount;
    }

    public void setAvailableTenantAccounts(List<TenantAccount> availableTenantAccounts) {
        this.availableTenantAccounts = availableTenantAccounts;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public void setRequiresTenantSelection(boolean requiresTenantSelection) {
        this.requiresTenantSelection = requiresTenantSelection;
    }

    public void setResultAuthentication(TenantAwareAuthenticationToken resultAuthentication) {
        this.resultAuthentication = resultAuthentication;
    }

    public User getUser() {
        return user;
    }

    /**
     * Convert the context state to an immutable AuthenticationResult.
     */
    public AuthenticationResult toResult() {
        return new AuthenticationResult(user, method, selectedTenantAccount,
                availableTenantAccounts != null ? availableTenantAccounts : List.of(),
                permissions != null ? permissions : Set.of(), requiresTenantSelection,
                LocalDateTime.now());
    }
}
