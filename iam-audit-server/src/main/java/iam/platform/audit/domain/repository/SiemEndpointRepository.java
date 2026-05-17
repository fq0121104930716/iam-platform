package iam.platform.audit.domain.repository;

import iam.platform.audit.domain.model.entity.SiemEndpoint;

import java.util.List;
import java.util.Optional;

/**
 * SIEM endpoint repository interface (domain layer).
 */
public interface SiemEndpointRepository {

    SiemEndpoint save(SiemEndpoint siemEndpoint);

    Optional<SiemEndpoint> findById(Long id);

    List<SiemEndpoint> findAllEnabled();

    List<SiemEndpoint> findAll();

    void deleteById(Long id);
}
