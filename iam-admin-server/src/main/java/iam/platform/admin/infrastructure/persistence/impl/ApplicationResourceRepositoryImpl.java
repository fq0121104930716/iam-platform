package iam.platform.admin.infrastructure.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import iam.platform.admin.domain.model.entity.ApplicationResource;
import iam.platform.admin.domain.repository.ApplicationResourceRepository;
import iam.platform.admin.infrastructure.persistence.converter.DomainPoMapper;
import iam.platform.admin.infrastructure.persistence.entity.ApplicationResourcePO;
import iam.platform.admin.infrastructure.persistence.repository.ApplicationResourceJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ApplicationResourceRepositoryImpl implements ApplicationResourceRepository {

    private final ApplicationResourceJpaRepository jpaRepository;
    private final DomainPoMapper domainPoMapper;

    @Override
    public ApplicationResource save(ApplicationResource resource) {
        ApplicationResourcePO po = domainPoMapper.toApplicationResourcePO(resource);
        ApplicationResourcePO savedPo = jpaRepository.save(po);
        return domainPoMapper.toApplicationResourceDomain(savedPo);
    }

    @Override
    public Optional<ApplicationResource> findById(Long id) {
        return jpaRepository.findById(id).map(domainPoMapper::toApplicationResourceDomain);
    }

    @Override
    public Optional<ApplicationResource> findByApplicationIdAndResourceCode(Long applicationId, String resourceCode) {
        return jpaRepository.findByApplicationIdAndResourceCode(applicationId, resourceCode)
                .map(domainPoMapper::toApplicationResourceDomain);
    }

    @Override
    public List<ApplicationResource> findByApplicationIdOrderBySortOrder(Long applicationId) {
        return jpaRepository.findByApplicationIdOrderBySortOrderAsc(applicationId).stream()
                .map(domainPoMapper::toApplicationResourceDomain).collect(Collectors.toList());
    }

    @Override
    public List<ApplicationResource> findByApplicationIdAndParentIdOrderBySortOrder(Long applicationId, Long parentId) {
        return jpaRepository.findByApplicationIdAndParentIdOrderBySortOrderAsc(applicationId, parentId).stream()
                .map(domainPoMapper::toApplicationResourceDomain).collect(Collectors.toList());
    }

    @Override
    public List<ApplicationResource> findByApplicationIdAndResourceTypeOrderBySortOrder(Long applicationId, String resourceType) {
        return jpaRepository.findByApplicationIdAndResourceTypeOrderBySortOrderAsc(applicationId, resourceType).stream()
                .map(domainPoMapper::toApplicationResourceDomain).collect(Collectors.toList());
    }

    @Override
    public Page<ApplicationResource> findByApplicationId(Long applicationId, Pageable pageable) {
        return jpaRepository.findAll(pageable).map(domainPoMapper::toApplicationResourceDomain);
    }

    @Override
    public boolean existsByApplicationIdAndResourceCode(Long applicationId, String resourceCode) {
        return jpaRepository.existsByApplicationIdAndResourceCode(applicationId, resourceCode);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteByApplicationId(Long applicationId) {
        jpaRepository.findAll().stream()
                .filter(po -> po.getApplicationId().equals(applicationId))
                .forEach(jpaRepository::delete);
    }
}
