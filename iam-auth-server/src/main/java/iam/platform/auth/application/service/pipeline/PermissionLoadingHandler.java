package iam.platform.auth.application.service.pipeline;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import iam.platform.auth.application.service.TenantAccountRoleApplicationService;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pipeline handler: loads permissions for the selected tenant account. Skipped if tenant selection
 * is required (no tenant selected yet).
 */
@Component
@RequiredArgsConstructor
public class PermissionLoadingHandler implements PostAuthHandler {

    private final TenantAccountRoleApplicationService tenantAccountRoleService;

    @Override
    public void handle(PostAuthContext context) {
        if (context.isRequiresTenantSelection() || context.getSelectedTenantAccount() == null) {
            // No tenant selected yet - permissions will be loaded after tenant selection
            context.setPermissions(Set.of());
            return;
        }

        Long tenantAccountId = context.getSelectedTenantAccount().getId();
        Set<String> permissions =
                tenantAccountRoleService.getTenantAccountPermissions(tenantAccountId).stream()
                        .map(p -> p.getPermissionCode()).collect(Collectors.toSet());
        context.setPermissions(permissions);
    }

    @Override
    public int getOrder() {
        return 400;
    }
}
