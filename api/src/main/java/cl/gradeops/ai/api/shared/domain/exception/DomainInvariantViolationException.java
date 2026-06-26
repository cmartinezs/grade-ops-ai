package cl.gradeops.ai.api.shared.domain.exception;

public class DomainInvariantViolationException extends DomainException {
    public DomainInvariantViolationException(String message) {
        super(message);
    }
}
