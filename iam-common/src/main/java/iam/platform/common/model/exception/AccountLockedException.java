package iam.platform.common.model.exception;

public class AccountLockedException extends BusinessException {
    private static final long serialVersionUID = 1L;

    public AccountLockedException(String message) {
        super("ACCOUNT_LOCKED", message, 403);
    }
}
