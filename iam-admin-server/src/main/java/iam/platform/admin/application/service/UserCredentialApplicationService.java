package iam.platform.admin.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import iam.platform.common.dto.request.ChangePasswordRequest;
import iam.platform.common.dto.request.CreateUserCredentialRequest;
import iam.platform.common.dto.request.UpdateUserCredentialRequest;
import iam.platform.common.dto.response.UserCredentialResponse;
import iam.platform.common.model.annotation.AuditLog;
import iam.platform.common.model.enums.AuditEventType;
import iam.platform.common.model.enums.CredentialType;
import iam.platform.common.model.enums.CredentialStatus;
import iam.platform.admin.domain.model.entity.UserCredential;
import iam.platform.admin.domain.repository.UserCredentialRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCredentialApplicationService {

    private final UserCredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @AuditLog(value = AuditEventType.USER_UPDATED, resourceType = "credential",
            action = "为用户 ID=#{#userId} 创建凭证")
    public UserCredentialResponse createCredential(Long userId,
            CreateUserCredentialRequest request) {

        CredentialType type = CredentialType.valueOf(request.getCredentialType());
        UserCredential credential = switch (type) {
            case PASSWORD -> UserCredential.createPassword(userId, request.getCredentialValue(),
                    passwordEncoder::encode);
            case CERTIFICATE -> UserCredential.createCertificate(userId,
                    request.getCredentialValue());
        };

        if (request.getAlgorithm() != null) {
            credential = credential.toBuilder().algorithm(request.getAlgorithm()).build();
        }
        if (request.getIsPrimary() != null) {
            credential = credential.toBuilder().isPrimary(request.getIsPrimary()).build();
        }
        if (request.getExpiresAt() != null) {
            credential = credential.toBuilder().expiresAt(request.getExpiresAt()).build();
        }
        if (request.getDescription() != null) {
            credential = credential.toBuilder().description(request.getDescription()).build();
        }

        credential = credentialRepository.save(credential);
        log.info("Credential created: {} for user {}", credential.getCredentialType(), userId);
        return toResponse(credential);
    }

    public UserCredentialResponse getCredential(Long credentialId) {
        UserCredential credential = credentialRepository.findById(credentialId).orElseThrow(
                () -> new IllegalArgumentException("Credential not found: " + credentialId));
        return toResponse(credential);
    }

    public Page<UserCredentialResponse> listCredentials(Long userId, Pageable pageable) {
        return credentialRepository.findByUserId(userId, pageable).map(this::toResponse);
    }

    @Transactional
    @AuditLog(value = AuditEventType.USER_UPDATED, resourceType = "credential",
            action = "更新凭证 ID=#{#credentialId}")
    public UserCredentialResponse updateCredential(Long userId, Long credentialId,
            UpdateUserCredentialRequest request) {
        UserCredential credential = credentialRepository.findById(credentialId).orElseThrow(
                () -> new IllegalArgumentException("Credential not found: " + credentialId));

        if (!credential.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Credential does not belong to user " + userId);
        }

        if (request.getCredentialValue() != null) {
            CredentialType type = credential.getCredentialType();
            if (type == CredentialType.PASSWORD) {
                String hashedValue = passwordEncoder.encode(request.getCredentialValue());
                credential = credential.toBuilder().credentialValue(hashedValue).build();
            } else {
                credential = credential.toBuilder().credentialValue(request.getCredentialValue())
                        .build();
            }
        }
        if (request.getExpiresAt() != null) {
            credential = credential.toBuilder().expiresAt(request.getExpiresAt()).build();
        }
        if (request.getDescription() != null) {
            credential = credential.toBuilder().description(request.getDescription()).build();
        }
        if (request.getIsPrimary() != null) {
            if (request.getIsPrimary()) {
                unsetOtherPrimaryCredentials(userId, credential.getCredentialType(), credentialId);
            }
            credential = credential.toBuilder().isPrimary(request.getIsPrimary()).build();
        }

        credential = credentialRepository.save(credential);
        log.info("Credential updated: {}", credentialId);
        return toResponse(credential);
    }

    @Transactional
    @AuditLog(value = AuditEventType.USER_UPDATED, resourceType = "credential",
            action = "设置主凭证 ID=#{#credentialId}")
    public void setPrimary(Long userId, Long credentialId) {
        UserCredential credential = credentialRepository.findById(credentialId).orElseThrow(
                () -> new IllegalArgumentException("Credential not found: " + credentialId));

        if (!credential.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Credential does not belong to user " + userId);
        }

        unsetOtherPrimaryCredentials(userId, credential.getCredentialType(), credentialId);
        credential.changePrimary(true);
        credentialRepository.save(credential);
        log.info("Credential set as primary: {}", credentialId);
    }

    @Transactional
    @AuditLog(value = AuditEventType.USER_UPDATED, resourceType = "credential",
            action = "吊销凭证 ID=#{#credentialId}")
    public void revokeCredential(Long userId, Long credentialId) {
        UserCredential credential = credentialRepository.findById(credentialId).orElseThrow(
                () -> new IllegalArgumentException("Credential not found: " + credentialId));

        if (!credential.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Credential does not belong to user " + userId);
        }

        ensureNotLastPasswordCredential(userId, credential);

        credential.revoke();
        credentialRepository.save(credential);
        log.info("Credential revoked: {}", credentialId);
    }

    @Transactional
    @AuditLog(value = AuditEventType.USER_UPDATED, resourceType = "credential",
            action = "删除凭证 ID=#{#credentialId}")
    public void deleteCredential(Long userId, Long credentialId) {
        UserCredential credential = credentialRepository.findById(credentialId).orElseThrow(
                () -> new IllegalArgumentException("Credential not found: " + credentialId));

        if (!credential.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Credential does not belong to user " + userId);
        }

        ensureNotLastPasswordCredential(userId, credential);

        credentialRepository.deleteById(credentialId);
        log.info("Credential deleted: {}", credentialId);
    }

    @Transactional
    @AuditLog(value = AuditEventType.USER_UPDATED, resourceType = "user",
            action = "修改用户 ID=#{#userId} 密码")
    public void changePassword(Long userId, ChangePasswordRequest request) {
        UserCredential credential =
                credentialRepository.findPrimaryByUserIdAndType(userId, CredentialType.PASSWORD)
                        .orElseThrow(() -> new IllegalStateException(
                                "No password credential found for user: " + userId));

        if (request.getOldPassword() != null) {
            boolean matches = passwordEncoder.matches(request.getOldPassword(),
                    credential.getCredentialValue());
            if (!matches) {
                throw new IllegalArgumentException("Old password is incorrect");
            }
        }

        String hashedValue = passwordEncoder.encode(request.getNewPassword());
        credential = credential.toBuilder().credentialValue(hashedValue).build();
        credentialRepository.save(credential);
        log.info("Password changed for user: {}", userId);
    }

    private void unsetOtherPrimaryCredentials(Long userId, CredentialType type, Long excludeId) {
        credentialRepository.findPrimaryByUserIdAndType(userId, type).ifPresent(primary -> {
            if (!primary.getId().equals(excludeId)) {
                primary.changePrimary(false);
                credentialRepository.save(primary);
            }
        });
    }

    private void ensureNotLastPasswordCredential(Long userId, UserCredential credential) {
        if (credential.getCredentialType() == CredentialType.PASSWORD) {
            List<UserCredential> passwordCredentials =
                    credentialRepository.findByUserIdAndType(userId, CredentialType.PASSWORD);
            long activeCount = passwordCredentials.stream()
                    .filter(c -> c.getStatus() == CredentialStatus.ACTIVE
                            && !c.getId().equals(credential.getId()))
                    .count();
            if (activeCount == 0) {
                throw new IllegalStateException(
                        "Cannot remove the last active password credential");
            }
        }
    }

    private UserCredentialResponse toResponse(UserCredential credential) {
        return UserCredentialResponse.builder().id(credential.getId())
                .userId(credential.getUserId())
                .credentialType(credential.getCredentialType().name())
                .algorithm(credential.getAlgorithm()).isPrimary(credential.isPrimary())
                .expiresAt(credential.getExpiresAt()).lastUsedAt(credential.getLastUsedAt())
                .status(credential.getStatus().name()).description(credential.getDescription())
                .createdAt(credential.getCreatedAt()).updatedAt(credential.getUpdatedAt())
                .createdBy(credential.getCreatedBy()).build();
    }
}
