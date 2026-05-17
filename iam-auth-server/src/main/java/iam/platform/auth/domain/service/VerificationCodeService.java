package iam.platform.auth.domain.service;

import iam.platform.auth.domain.model.entity.User;

public interface VerificationCodeService {
    // 生成并发送验证码
    void sendSmsCode(String phone);

    void sendEmailCode(String email);

    // 验证验证码
    boolean verifySmsCode(String phone, String code);

    boolean verifyEmailCode(String email, String code);

    // 查找或创建用户
    User findOrCreateUserByPhone(String phone);

    User findOrCreateUserByEmail(String email);
}
