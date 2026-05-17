package iam.platform.common.model.exception;

public class ConflictException extends BusinessException {
    private static final long serialVersionUID = 1L;

    public ConflictException(String message) {
        super("CONFLICT", message, 409);
    }
}
