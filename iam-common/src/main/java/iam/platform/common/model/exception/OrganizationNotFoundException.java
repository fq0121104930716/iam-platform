package iam.platform.common.model.exception;

public class OrganizationNotFoundException extends BusinessException {
    private static final long serialVersionUID = 1L;

    public OrganizationNotFoundException(String message) {
        super("ORGANIZATION_NOT_FOUND", message, 404);
    }
}
