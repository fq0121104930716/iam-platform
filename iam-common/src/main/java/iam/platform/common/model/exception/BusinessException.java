package iam.platform.common.model.exception;

import lombok.Getter;

@Getter
public abstract class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final String errorCode;
    private final int httpStatus;

    protected BusinessException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
