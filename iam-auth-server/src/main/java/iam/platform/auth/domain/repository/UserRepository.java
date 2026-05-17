package iam.platform.auth.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import iam.platform.auth.domain.model.entity.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Page<User> findAll(Pageable pageable);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    void deleteById(Long id);

    long countByEnabledTrue();

    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}
