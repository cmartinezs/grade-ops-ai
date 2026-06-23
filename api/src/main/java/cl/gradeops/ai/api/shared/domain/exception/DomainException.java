package cl.gradeops.ai.api.shared.domain.exception;

public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }
}
