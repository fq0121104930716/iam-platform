package iam.platform.common.model.exception;

public class TenantAccountNotFoundException extends BusinessException {
    private static final long serialVersionUID = 1L;

    public TenantAccountNotFoundException(String message) {
        super("TENANT_ACCOUNT_NOT_FOUND", message, 404);
    }
}
