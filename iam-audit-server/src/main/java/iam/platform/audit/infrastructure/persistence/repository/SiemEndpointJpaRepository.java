package iam.platform.audit.infrastructure.persistence.repository;

import iam.platform.audit.infrastructure.persistence.entity.SiemEndpointPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * SIEM endpoint JPA repository.
 */
public interface SiemEndpointJpaRepository extends JpaRepository<SiemEndpointPO, Long> {

    List<SiemEndpointPO> findByEnabledTrue();
}
