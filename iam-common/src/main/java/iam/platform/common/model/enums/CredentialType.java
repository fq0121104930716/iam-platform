package iam.platform.common.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CredentialType {
    PASSWORD("本地密码认证"),
    CERTIFICATE("数字证书认证");

    private final String description;
}
