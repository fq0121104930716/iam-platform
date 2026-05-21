package iam.platform.admin.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import iam.platform.admin.infrastructure.persistence.entity.UserCredentialPO;
import iam.platform.common.model.enums.CredentialType;
import iam.platform.common.model.enums.CredentialStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserCredentialJpaRepository extends JpaRepository<UserCredentialPO, Long> {

    List<UserCredentialPO> findByUserId(Long userId);

    List<UserCredentialPO> findByUserIdAndCredentialType(Long userId, CredentialType credentialType);

    Optional<UserCredentialPO> findByUserIdAndCredentialTypeAndIsPrimaryTrue(Long userId, CredentialType credentialType);

    List<UserCredentialPO> findByUserIdAndStatus(Long userId, CredentialStatus status);

    Page<UserCredentialPO> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT c FROM UserCredentialPO c WHERE c.status = 'ACTIVE' AND c.expiresAt IS NOT NULL AND c.expiresAt < :now")
    List<UserCredentialPO> findExpiredCredentials(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM UserCredentialPO c WHERE c.userId = :userId AND c.credentialType = :type AND c.isPrimary = true AND c.status = 'ACTIVE' AND (c.expiresAt IS NULL OR c.expiresAt > :now)")
    Optional<UserCredentialPO> findActivePrimaryCredential(@Param("userId") Long userId,
                                                          @Param("type") CredentialType type,
                                                          @Param("now") LocalDateTime now);
}
