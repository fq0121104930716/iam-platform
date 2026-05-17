package iam.platform.auth.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import iam.platform.auth.infrastructure.persistence.entity.PersonPO;

import java.util.Optional;

public interface PersonJpaRepository extends JpaRepository<PersonPO, Long> {
    Optional<PersonPO> findByUsername(String username);

    Optional<PersonPO> findByEmail(String email);

    Optional<PersonPO> findByPhone(String phone);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    long countByEnabledTrue();

    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
