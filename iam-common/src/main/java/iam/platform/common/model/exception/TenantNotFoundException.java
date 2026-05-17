package iam.platform.common.model.exception;

public class TenantNotFoundException extends BusinessException {
    private static final long serialVersionUID = 1L;

    public TenantNotFoundException(String message) {
        super("TENANT_NOT_FOUND", message, 404);
    }
}
