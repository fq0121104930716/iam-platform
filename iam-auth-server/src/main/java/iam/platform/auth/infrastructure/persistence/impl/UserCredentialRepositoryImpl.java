package iam.platform.auth.infrastructure.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import iam.platform.auth.domain.repository.UserCredentialRepository;
import iam.platform.auth.infrastructure.persistence.repository.UserCredentialJpaRepository;
import iam.platform.common.model.enums.CredentialType;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserCredentialRepositoryImpl implements UserCredentialRepository {

    private final UserCredentialJpaRepository jpaRepository;

    @Override
    public Optional<CredentialInfo> findPrimaryCredential(Long userId, CredentialType type) {
        return jpaRepository.findActivePrimaryCredential(userId, type, LocalDateTime.now())
                .map(po -> new CredentialInfo(po.getId(), po.getCredentialValue()));
    }

    @Override
    public void updateLastUsed(Long credentialId) {
        jpaRepository.updateLastUsedAt(credentialId, LocalDateTime.now());
    }
}
