package cl.gradeops.ai.api.auth.domain.exception;

import cl.gradeops.ai.api.shared.domain.exception.DomainException;

public class ResetCodeEmailMismatchException extends DomainException {
    public ResetCodeEmailMismatchException() {
        super("Email does not match the reset code owner");
    }
}
