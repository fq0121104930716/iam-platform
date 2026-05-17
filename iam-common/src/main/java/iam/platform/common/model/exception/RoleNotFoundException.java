package iam.platform.common.model.exception;

public class RoleNotFoundException extends BusinessException {
    private static final long serialVersionUID = 1L;

    public RoleNotFoundException(String message) {
        super("ROLE_NOT_FOUND", message, 404);
    }
}
