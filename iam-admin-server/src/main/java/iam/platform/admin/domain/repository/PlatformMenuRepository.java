package iam.platform.admin.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import iam.platform.admin.domain.model.entity.PlatformMenu;

import java.util.List;
import java.util.Optional;

public interface PlatformMenuRepository {
    PlatformMenu save(PlatformMenu menu);

    Optional<PlatformMenu> findById(Long id);

    Optional<PlatformMenu> findByMenuCode(String menuCode);

    List<PlatformMenu> findAllOrderBySortOrder();

    List<PlatformMenu> findByParentIdOrderBySortOrder(Long parentId);

    Page<PlatformMenu> findAll(Pageable pageable);

    boolean existsByMenuCode(String menuCode);

    void deleteById(Long id);
}
