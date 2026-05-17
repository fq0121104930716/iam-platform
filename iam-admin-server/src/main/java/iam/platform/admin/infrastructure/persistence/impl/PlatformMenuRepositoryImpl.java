package iam.platform.admin.infrastructure.persistence.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import iam.platform.admin.domain.model.entity.PlatformMenu;
import iam.platform.admin.domain.repository.PlatformMenuRepository;
import iam.platform.admin.infrastructure.persistence.converter.DomainPoMapper;
import iam.platform.admin.infrastructure.persistence.entity.PlatformMenuPO;
import iam.platform.admin.infrastructure.persistence.repository.PlatformMenuJpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PlatformMenuRepositoryImpl implements PlatformMenuRepository {

    private final PlatformMenuJpaRepository jpaRepository;
    private final DomainPoMapper domainPoMapper;

    @Override
    public PlatformMenu save(PlatformMenu menu) {
        PlatformMenuPO po = domainPoMapper.toPlatformMenuPO(menu);
        PlatformMenuPO savedPo = jpaRepository.save(po);
        return domainPoMapper.toPlatformMenuDomain(savedPo);
    }

    @Override
    public Optional<PlatformMenu> findById(Long id) {
        return jpaRepository.findById(id).map(domainPoMapper::toPlatformMenuDomain);
    }

    @Override
    public Optional<PlatformMenu> findByMenuCode(String menuCode) {
        return jpaRepository.findByMenuCode(menuCode).map(domainPoMapper::toPlatformMenuDomain);
    }

    @Override
    public List<PlatformMenu> findAllOrderBySortOrder() {
        return jpaRepository.findAllByOrderBySortOrderAsc().stream()
                .map(domainPoMapper::toPlatformMenuDomain).toList();
    }

    @Override
    public List<PlatformMenu> findByParentIdOrderBySortOrder(Long parentId) {
        return jpaRepository.findByParentIdOrderBySortOrderAsc(parentId).stream()
                .map(domainPoMapper::toPlatformMenuDomain).toList();
    }

    @Override
    public Page<PlatformMenu> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(domainPoMapper::toPlatformMenuDomain);
    }

    @Override
    public boolean existsByMenuCode(String menuCode) {
        return jpaRepository.existsByMenuCode(menuCode);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
