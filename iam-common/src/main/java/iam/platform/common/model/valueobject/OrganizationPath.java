package iam.platform.common.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

/**
 * Value object representing the materialized path of an organization in the
 * hierarchy.
 * Path format: "/{tenantId}/{orgId1}/{orgId2}/..."
 */
@Getter
@EqualsAndHashCode
public class OrganizationPath implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String value;

    private OrganizationPath(String value) {
        this.value = value;
    }

    /**
     * Create a root organization path for a tenant.
     */
    public static OrganizationPath root(Long tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException("Tenant ID cannot be null");
        }
        return new OrganizationPath("/" + tenantId);
    }

    /**
     * Reconstitute an OrganizationPath from a stored value.
     */
    public static OrganizationPath of(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Organization path cannot be blank");
        }
        return new OrganizationPath(raw);
    }

    /**
     * Create a child path by appending a child ID.
     */
    public OrganizationPath childPath(Long childId) {
        if (childId == null) {
            throw new IllegalArgumentException("Child ID cannot be null");
        }
        return new OrganizationPath(this.value + "/" + childId);
    }

    /**
     * Check if this path is an ancestor of another path.
     */
    public boolean isAncestorOf(OrganizationPath other) {
        if (other == null) {
            return false;
        }
        return other.value.startsWith(this.value + "/");
    }

    /**
     * Reparent this path by replacing the old prefix with a new prefix.
     */
    public OrganizationPath reparent(OrganizationPath oldPrefix, OrganizationPath newPrefix) {
        if (!this.value.startsWith(oldPrefix.value)) {
            throw new IllegalArgumentException(
                    "Path does not start with old prefix: " + oldPrefix.value);
        }
        String suffix = this.value.substring(oldPrefix.value.length());
        return new OrganizationPath(newPrefix.value + suffix);
    }

    /**
     * Calculate the level (depth) based on path separators.
     * Root level (e.g., "/1") = 0, first child (e.g., "/1/2") = 1.
     */
    public int calculateLevel() {
        // Count segments after splitting by "/", minus tenant root
        String[] segments = value.split("/");
        // segments[0] is empty (before first /), segments[1] is tenantId
        return segments.length - 2;
    }

    @Override
    public String toString() {
        return value;
    }
}
