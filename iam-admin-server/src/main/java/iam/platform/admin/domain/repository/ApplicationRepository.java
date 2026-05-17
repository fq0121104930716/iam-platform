package iam.platform.admin.domain.repository;

import iam.platform.admin.domain.model.entity.Application;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository {
    Application save(Application app);

    Optional<Application> findById(Long id);

    Optional<Application> findByAppId(String appId);

    List<Application> findByTenantId(Long tenantId);

    List<Application> findAll();

    void deleteById(Long id);

    long countByTenantIdAndStatus(Long tenantId, String status);

    long countByStatus(String status);
}
