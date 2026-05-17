package iam.platform.admin.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import iam.platform.admin.infrastructure.persistence.entity.UserExternalLoginPO;

import java.util.List;
import java.util.Optional;

public interface UserExternalLoginJpaRepository
        extends JpaRepository<UserExternalLoginPO, Long> {

    List<UserExternalLoginPO> findByUserId(Long userId);

    Optional<UserExternalLoginPO> findByProviderAndProviderUserId(String provider,
            String providerUserId);

    boolean existsByUserIdAndProvider(Long userId, String provider);
}
