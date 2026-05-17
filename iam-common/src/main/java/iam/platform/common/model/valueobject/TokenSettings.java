package iam.platform.common.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

/**
 * Value object representing OAuth2 token TTL settings.
 */
@Getter
@EqualsAndHashCode
public class TokenSettings implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_ACCESS_TOKEN_TTL = 3600;
    private static final int DEFAULT_REFRESH_TOKEN_TTL = 86400;

    private final int accessTokenTtlSeconds;
    private final int refreshTokenTtlSeconds;

    private TokenSettings(int accessTokenTtlSeconds, int refreshTokenTtlSeconds) {
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
    }

    /**
     * Create TokenSettings with defaults applied for null values.
     * Access token default: 3600 seconds (1 hour).
     * Refresh token default: 86400 seconds (24 hours).
     *
     * @param accessTtl  access token TTL in seconds, null for default
     * @param refreshTtl refresh token TTL in seconds, null for default
     */
    public static TokenSettings of(Integer accessTtl, Integer refreshTtl) {
        int access = accessTtl != null ? accessTtl : DEFAULT_ACCESS_TOKEN_TTL;
        int refresh = refreshTtl != null ? refreshTtl : DEFAULT_REFRESH_TOKEN_TTL;
        if (access <= 0) {
            throw new IllegalArgumentException(
                    "Access token TTL must be positive, got: " + access);
        }
        if (refresh <= 0) {
            throw new IllegalArgumentException(
                    "Refresh token TTL must be positive, got: " + refresh);
        }
        return new TokenSettings(access, refresh);
    }

    /**
     * Create with explicit values (for reconstitution from database).
     */
    public static TokenSettings ofExact(int accessTtl, int refreshTtl) {
        return new TokenSettings(accessTtl, refreshTtl);
    }

    @Override
    public String toString() {
        return "TokenSettings[access=" + accessTokenTtlSeconds
                + "s, refresh=" + refreshTokenTtlSeconds + "s]";
    }
}
