package iam.platform.admin.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import iam.platform.admin.infrastructure.persistence.entity.PlatformMenuPO;

import java.util.List;
import java.util.Optional;

public interface PlatformMenuJpaRepository extends JpaRepository<PlatformMenuPO, Long> {
    Optional<PlatformMenuPO> findByMenuCode(String menuCode);

    List<PlatformMenuPO> findAllByOrderBySortOrderAsc();

    List<PlatformMenuPO> findByParentIdOrderBySortOrderAsc(Long parentId);

    boolean existsByMenuCode(String menuCode);
}
