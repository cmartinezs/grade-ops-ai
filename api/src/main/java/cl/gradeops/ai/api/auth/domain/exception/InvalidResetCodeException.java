package cl.gradeops.ai.api.auth.domain.exception;

import cl.gradeops.ai.api.shared.domain.exception.DomainException;

public class InvalidResetCodeException extends DomainException {
    public InvalidResetCodeException(String reason) {
        super("Invalid reset code: " + reason);
    }
}
