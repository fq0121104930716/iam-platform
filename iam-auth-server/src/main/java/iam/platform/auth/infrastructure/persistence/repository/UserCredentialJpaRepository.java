package iam.platform.auth.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import iam.platform.auth.infrastructure.persistence.entity.UserCredentialPO;
import iam.platform.common.model.enums.CredentialType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserCredentialJpaRepository extends JpaRepository<UserCredentialPO, Long> {

    @Query("SELECT c FROM UserCredentialPO c WHERE c.userId = :userId AND c.credentialType = :type AND c.isPrimary = true AND c.status = 'ACTIVE' AND (c.expiresAt IS NULL OR c.expiresAt > :now)")
    Optional<UserCredentialPO> findActivePrimaryCredential(@Param("userId") Long userId,
                                                          @Param("type") CredentialType type,
                                                          @Param("now") LocalDateTime now);

    List<UserCredentialPO> findByUserIdAndCredentialType(Long userId, CredentialType credentialType);

    @Query("SELECT c FROM UserCredentialPO c WHERE c.status = 'ACTIVE' AND c.expiresAt IS NOT NULL AND c.expiresAt < :now")
    List<UserCredentialPO> findExpiredCredentials(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE UserCredentialPO c SET c.lastUsedAt = :now, c.updatedAt = :now WHERE c.id = :id")
    void updateLastUsedAt(@Param("id") Long id, @Param("now") LocalDateTime now);
}
