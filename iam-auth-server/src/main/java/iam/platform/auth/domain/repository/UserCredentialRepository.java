package iam.platform.auth.domain.repository;

import iam.platform.common.model.enums.CredentialType;

import java.util.Optional;

public interface UserCredentialRepository {
    record CredentialInfo(Long id, String credentialValue) {}
    
    Optional<CredentialInfo> findPrimaryCredential(Long userId, CredentialType type);

    void updateLastUsed(Long credentialId);
}
