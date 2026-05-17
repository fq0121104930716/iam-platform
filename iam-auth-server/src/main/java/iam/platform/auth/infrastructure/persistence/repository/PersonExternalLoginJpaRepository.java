package iam.platform.auth.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import iam.platform.auth.infrastructure.persistence.entity.PersonExternalLoginPO;

import java.util.List;
import java.util.Optional;

public interface PersonExternalLoginJpaRepository
        extends JpaRepository<PersonExternalLoginPO, Long> {

    List<PersonExternalLoginPO> findByPersonId(Long personId);

    Optional<PersonExternalLoginPO> findByProviderAndProviderUserId(String provider,
            String providerUserId);

    boolean existsByPersonIdAndProvider(Long personId, String provider);
}
