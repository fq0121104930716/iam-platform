package iam.platform.auth.domain.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import iam.platform.auth.domain.model.entity.User;
import iam.platform.auth.domain.repository.UserRepository;
import iam.platform.auth.domain.service.VerificationCodeService;

import java.security.SecureRandom;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;

    private static final Duration CODE_EXPIRE = Duration.ofMinutes(5);
    private static final int CODE_LENGTH = 6;
    private static final Duration RATE_LIMIT_DURATION = Duration.ofSeconds(60);
    private static final int MAX_DAILY_SEND_COUNT = 10;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public void sendSmsCode(String phone) {
        // Rate limiting check
        checkRateLimit("sms:rate:" + phone, "SMS");

        String code = generateCode();
        String key = "sms:code:" + phone;
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRE);

        // TODO: 集成真实的 SMS 服务（如阿里云、腾讯云）
        // 开发环境下打印到日志，生产环境应调用 SMS API
        log.info("SMS verification code sent to {}: {}", phone, code);
    }

    @Override
    public void sendEmailCode(String email) {
        // Rate limiting check
        checkRateLimit("email:rate:" + email, "Email");

        String code = generateCode();
        String key = "email:code:" + email;
        redisTemplate.opsForValue().set(key, code, CODE_EXPIRE);

        // TODO: 集成真实的邮件服务（如 Spring Mail）
        // 开发环境下打印到日志，生产环境应发送邮件
        log.info("Email verification code sent to {}: {}", email, code);
    }

    @Override
    public boolean verifySmsCode(String phone, String code) {
        String key = "sms:code:" + phone;
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            log.warn("SMS verification code expired or not found for phone: {}", phone);
            return false;
        }

        if (storedCode.equals(code)) {
            redisTemplate.delete(key); // 验证成功后立即删除
            log.info("SMS verification code verified successfully for phone: {}", phone);
            return true;
        }

        log.warn("SMS verification code mismatch for phone: {}", phone);
        return false;
    }

    @Override
    public boolean verifyEmailCode(String email, String code) {
        String key = "email:code:" + email;
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            log.warn("Email verification code expired or not found for email: {}", email);
            return false;
        }

        if (storedCode.equals(code)) {
            redisTemplate.delete(key);
            log.info("Email verification code verified successfully for email: {}", email);
            return true;
        }

        log.warn("Email verification code mismatch for email: {}", email);
        return false;
    }

    @Override
    public User findOrCreateUserByPhone(String phone) {
        return userRepository.findByPhone(phone).orElseGet(() -> {
            User newUser = User.builder().phone(phone)
                    .username("user_" + phone.substring(phone.length() - 4)).phoneVerified(true)
                    .enabled(true).build();
            User savedUser = userRepository.save(newUser);
            log.info("Created new User from SMS login: {}", savedUser.getUsername());
            return savedUser;
        });
    }

    @Override
    public User findOrCreateUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            String username = email.substring(0, email.indexOf('@'));
            User newUser = User.builder().email(email).username(username).emailVerified(true)
                    .enabled(true).build();
            User savedUser = userRepository.save(newUser);
            log.info("Created new User from Email login: {}", savedUser.getUsername());
            return savedUser;
        });
    }

    private String generateCode() {
        return String.format("%0" + CODE_LENGTH + "d", SECURE_RANDOM.nextInt(1000000));
    }

    /**
     * Check rate limit for verification code sending. Prevents abuse by limiting the frequency of
     * code requests.
     */
    private void checkRateLimit(String rateLimitKey, String type) {
        // Check if rate limit exists
        Boolean exists = redisTemplate.hasKey(rateLimitKey);
        if (Boolean.TRUE.equals(exists)) {
            throw new IllegalStateException(
                    type + " verification code can only be sent once per minute");
        }

        // Check daily limit
        String dailyLimitKey =
                rateLimitKey.replace(":rate:", ":daily:") + ":" + java.time.LocalDate.now();
        String dailyCountStr = redisTemplate.opsForValue().get(dailyLimitKey);
        int dailyCount = dailyCountStr != null ? Integer.parseInt(dailyCountStr) : 0;

        if (dailyCount >= MAX_DAILY_SEND_COUNT) {
            throw new IllegalStateException("Daily " + type.toLowerCase()
                    + " verification code limit reached (" + MAX_DAILY_SEND_COUNT + " codes)");
        }

        // Set rate limit (1 minute)
        redisTemplate.opsForValue().set(rateLimitKey, "1", RATE_LIMIT_DURATION);

        // Increment daily counter
        redisTemplate.opsForValue().set(dailyLimitKey, String.valueOf(dailyCount + 1),
                Duration.ofDays(1));
    }
}
