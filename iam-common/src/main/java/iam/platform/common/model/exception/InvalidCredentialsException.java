package iam.platform.common.model.exception;

public class InvalidCredentialsException extends BusinessException {
    private static final long serialVersionUID = 1L;

    public InvalidCredentialsException(String message) {
        super("INVALID_CREDENTIALS", message, 401);
    }
}
