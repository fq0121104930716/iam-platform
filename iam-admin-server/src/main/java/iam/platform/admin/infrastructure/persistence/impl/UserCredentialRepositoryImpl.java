package iam.platform.admin.infrastructure.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import iam.platform.admin.domain.model.entity.UserCredential;
import iam.platform.admin.domain.repository.UserCredentialRepository;
import iam.platform.admin.infrastructure.persistence.entity.UserCredentialPO;
import iam.platform.admin.infrastructure.persistence.repository.UserCredentialJpaRepository;
import iam.platform.common.model.enums.CredentialType;
import iam.platform.common.model.enums.CredentialStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserCredentialRepositoryImpl implements UserCredentialRepository {

    private final UserCredentialJpaRepository jpaRepository;

    @Override
    public UserCredential save(UserCredential credential) {
        UserCredentialPO po = toPO(credential);
        UserCredentialPO savedPo = jpaRepository.save(po);
        return toDomain(savedPo);
    }

    @Override
    public Optional<UserCredential> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<UserCredential> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<UserCredential> findByUserIdAndType(Long userId, CredentialType type) {
        return jpaRepository.findByUserIdAndCredentialType(userId, type).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public Optional<UserCredential> findPrimaryByUserIdAndType(Long userId, CredentialType type) {
        return jpaRepository.findByUserIdAndCredentialTypeAndIsPrimaryTrue(userId, type)
                .map(this::toDomain);
    }

    @Override
    public List<UserCredential> findByUserIdAndStatus(Long userId, CredentialStatus status) {
        return jpaRepository.findByUserIdAndStatus(userId, status).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public Page<UserCredential> findByUserId(Long userId, Pageable pageable) {
        return jpaRepository.findByUserId(userId, pageable).map(this::toDomain);
    }

    @Override
    public List<UserCredential> findExpiredCredentials() {
        return jpaRepository.findExpiredCredentials(LocalDateTime.now()).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private UserCredentialPO toPO(UserCredential credential) {
        UserCredentialPO po = new UserCredentialPO();
        po.setId(credential.getId());
        po.setUserId(credential.getUserId());
        po.setCredentialType(credential.getCredentialType());
        po.setCredentialValue(credential.getCredentialValue());
        po.setAlgorithm(credential.getAlgorithm());
        po.setPrimary(credential.isPrimary());
        po.setExpiresAt(credential.getExpiresAt());
        po.setLastUsedAt(credential.getLastUsedAt());
        po.setStatus(credential.getStatus());
        po.setDescription(credential.getDescription());
        po.setCreatedAt(credential.getCreatedAt());
        po.setUpdatedAt(credential.getUpdatedAt());
        po.setCreatedBy(credential.getCreatedBy());
        return po;
    }

    private UserCredential toDomain(UserCredentialPO po) {
        return UserCredential.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .credentialType(po.getCredentialType())
                .credentialValue(po.getCredentialValue())
                .algorithm(po.getAlgorithm())
                .isPrimary(po.isPrimary())
                .expiresAt(po.getExpiresAt())
                .lastUsedAt(po.getLastUsedAt())
                .status(po.getStatus())
                .description(po.getDescription())
                .createdAt(po.getCreatedAt())
                .updatedAt(po.getUpdatedAt())
                .createdBy(po.getCreatedBy())
                .build();
    }
}
