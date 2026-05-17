package iam.platform.auth.domain.service;

import iam.platform.auth.domain.model.entity.Person;
import iam.platform.auth.domain.model.valueobject.TenantResolutionResult;

/**
 * Domain service for resolving tenant context after authentication. Determines which tenant account
 * to use based on user's associations and request context.
 */
public interface TenantResolutionPolicy {

    /**
     * Resolve tenant context for an authenticated person.
     *
     * @param person the authenticated person
     * @param requestedTenantCode optional tenant code from request (header/param/subdomain); null
     *        to auto-resolve
     * @return resolution result indicating selected account or that UI selection is needed
     */
    TenantResolutionResult resolve(Person person, String requestedTenantCode);
}
