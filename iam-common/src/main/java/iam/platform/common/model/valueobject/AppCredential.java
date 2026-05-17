package iam.platform.common.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.util.UUID;

/**
 * Value object representing OAuth2 application credentials (appId + appSecret).
 */
@Getter
@EqualsAndHashCode
public class AppCredential implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String appId;
    private final String appSecret;

    private AppCredential(String appId, String appSecret) {
        this.appId = appId;
        this.appSecret = appSecret;
    }

    /**
     * Generate new application credentials.
     * AppId: 16-character alphanumeric string.
     * AppSecret: 64-character alphanumeric string.
     */
    public static AppCredential generate() {
        String appId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String appSecret = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        return new AppCredential(appId, appSecret);
    }

    /**
     * Reconstitute credentials from stored values.
     */
    public static AppCredential of(String appId, String appSecret) {
        if (appId == null || appId.isBlank()) {
            throw new IllegalArgumentException("AppId cannot be blank");
        }
        if (appSecret == null || appSecret.isBlank()) {
            throw new IllegalArgumentException("AppSecret cannot be blank");
        }
        return new AppCredential(appId, appSecret);
    }

    /**
     * Rotate the secret while keeping the same appId.
     *
     * @return a new AppCredential with the same appId but a new secret
     */
    public AppCredential rotateSecret() {
        String newSecret = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        return new AppCredential(this.appId, newSecret);
    }

    @Override
    public String toString() {
        return "AppCredential[appId=" + appId + ", secret=PROTECTED]";
    }
}
