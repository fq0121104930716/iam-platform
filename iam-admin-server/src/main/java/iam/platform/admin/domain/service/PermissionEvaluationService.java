package iam.platform.admin.domain.service;

import java.util.Set;

/**
 * 权限计算引擎领域服务接口
 */
public interface PermissionEvaluationService {

    /**
     * 检查用户是否拥有指定权限
     *
     * @param tenantAccountId 租户账号ID
     * @param permissionCode 权限码（如 "user:read"）
     * @return 是否拥有权限
     */
    boolean hasPermission(Long tenantAccountId, String permissionCode);

    /**
     * 检查用户是否拥有任一权限
     *
     * @param tenantAccountId 租户账号ID
     * @param permissionCodes 权限码列表
     * @return 是否拥有任一权限
     */
    boolean hasAnyPermission(Long tenantAccountId, Set<String> permissionCodes);

    /**
     * 检查用户是否拥有所有权限
     *
     * @param tenantAccountId 租户账号ID
     * @param permissionCodes 权限码列表
     * @return 是否拥有所有权限
     */
    boolean hasAllPermissions(Long tenantAccountId, Set<String> permissionCodes);

    /**
     * 获取用户的所有权限码
     *
     * @param tenantAccountId 租户账号ID
     * @return 权限码集合
     */
    Set<String> getAllPermissions(Long tenantAccountId);

    /**
     * 校验权限，如果没有权限则抛出异常
     *
     * @param tenantAccountId 租户账号ID
     * @param permissionCode 权限码
     * @throws iam.platform.common.model.exception.AccessDeniedException 如果没有权限
     */
    void checkPermission(Long tenantAccountId, String permissionCode);
}
