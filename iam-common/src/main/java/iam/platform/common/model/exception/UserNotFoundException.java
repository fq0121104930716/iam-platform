package iam.platform.common.model.exception;

public class UserNotFoundException extends BusinessException {
    private static final long serialVersionUID = 1L;

    public UserNotFoundException(String message) {
        super("USER_NOT_FOUND", message, 404);
    }
}
