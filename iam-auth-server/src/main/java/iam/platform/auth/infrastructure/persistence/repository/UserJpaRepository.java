package iam.platform.auth.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import iam.platform.auth.infrastructure.persistence.entity.UserPO;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserPO, Long> {
    Optional<UserPO> findByUsername(String username);

    Optional<UserPO> findByEmail(String email);

    Optional<UserPO> findByPhone(String phone);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    long countByEnabledTrue();

    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
