package iam.platform.auth.application.service.pipeline;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import iam.platform.auth.domain.model.valueobject.TenantResolutionResult;
import iam.platform.auth.domain.service.TenantResolutionPolicy;

import java.util.List;

/**
 * Pipeline handler: resolves tenant context from request parameters and user's tenant accounts.
 * Extracts tenant code from request header/param, then delegates to TenantResolutionPolicy.
 */
@Component
@RequiredArgsConstructor
public class TenantResolutionHandler implements PostAuthHandler {

    private static final String HEADER_TENANT_CODE = "X-Tenant-Code";
    private static final String PARAM_TENANT = "tenant";

    private final TenantResolutionPolicy tenantResolutionPolicy;

    @Override
    public void handle(PostAuthContext context) {
        String tenantCode = extractTenantCode(context);
        TenantResolutionResult result =
                tenantResolutionPolicy.resolve(context.getUser(), tenantCode);

        switch (result.status()) {
            case AUTO_SELECTED -> {
                context.setSelectedTenantAccount(result.selectedAccount());
                context.setAvailableTenantAccounts(result.availableAccounts());
                context.setRequiresTenantSelection(false);
            }
            case SELECTION_REQUIRED -> {
                context.setSelectedTenantAccount(null);
                context.setAvailableTenantAccounts(result.availableAccounts());
                context.setRequiresTenantSelection(true);
            }
            case NO_ACCOUNTS -> {
                context.setSelectedTenantAccount(null);
                context.setAvailableTenantAccounts(List.of());
                context.setRequiresTenantSelection(false);
            }
        }
    }

    private String extractTenantCode(PostAuthContext context) {
        // Priority: header > query param
        String tenantCode = context.getRequest().getHeader(HEADER_TENANT_CODE);
        if (tenantCode != null && !tenantCode.isBlank()) {
            return tenantCode.trim();
        }
        tenantCode = context.getRequest().getParameter(PARAM_TENANT);
        if (tenantCode != null && !tenantCode.isBlank()) {
            return tenantCode.trim();
        }
        return null;
    }

    @Override
    public int getOrder() {
        return 300;
    }
}
