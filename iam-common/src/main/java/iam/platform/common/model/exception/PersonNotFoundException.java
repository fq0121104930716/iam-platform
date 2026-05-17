package iam.platform.common.model.exception;

public class PersonNotFoundException extends BusinessException {
    private static final long serialVersionUID = 1L;

    public PersonNotFoundException(String message) {
        super("PERSON_NOT_FOUND", message, 404);
    }
}
