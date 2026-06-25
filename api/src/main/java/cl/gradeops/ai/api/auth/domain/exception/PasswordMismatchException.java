package cl.gradeops.ai.api.auth.domain.exception;

import cl.gradeops.ai.api.shared.domain.exception.DomainException;

public class PasswordMismatchException extends DomainException {
    public PasswordMismatchException() {
        super("Passwords do not match");
    }
}
