package iam.platform.admin.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import iam.platform.admin.domain.model.entity.UserCredential;
import iam.platform.common.model.enums.CredentialType;
import iam.platform.common.model.enums.CredentialStatus;

import java.util.List;
import java.util.Optional;

public interface UserCredentialRepository {
    UserCredential save(UserCredential credential);

    Optional<UserCredential> findById(Long id);

    List<UserCredential> findByUserId(Long userId);

    List<UserCredential> findByUserIdAndType(Long userId, CredentialType type);

    Optional<UserCredential> findPrimaryByUserIdAndType(Long userId, CredentialType type);

    List<UserCredential> findByUserIdAndStatus(Long userId, CredentialStatus status);

    Page<UserCredential> findByUserId(Long userId, Pageable pageable);

    List<UserCredential> findExpiredCredentials();

    void deleteById(Long id);
}
