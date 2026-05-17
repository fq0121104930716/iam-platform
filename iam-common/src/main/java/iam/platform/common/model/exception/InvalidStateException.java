package iam.platform.common.model.exception;

public class InvalidStateException extends BusinessException {
    private static final long serialVersionUID = 1L;

    public InvalidStateException(String message) {
        super("INVALID_STATE", message, 400);
    }
}
