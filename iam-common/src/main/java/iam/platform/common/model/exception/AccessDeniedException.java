package iam.platform.common.model.exception;

/**
 * 访问拒绝异常 - 当用户没有所需权限时抛出
 */
public class AccessDeniedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String permissionCode;

    public AccessDeniedException(String permissionCode) {
        super("Access denied: required permission '" + permissionCode + "' not granted");
        this.permissionCode = permissionCode;
    }

    public AccessDeniedException(String message, String permissionCode) {
        super(message);
        this.permissionCode = permissionCode;
    }

    public String getPermissionCode() {
        return permissionCode;
    }
}
