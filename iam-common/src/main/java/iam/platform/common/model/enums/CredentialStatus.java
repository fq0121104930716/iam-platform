package iam.platform.common.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CredentialStatus {
    ACTIVE("可用"),
    EXPIRED("已过期"),
    REVOKED("已吊销");

    private final String description;
}
