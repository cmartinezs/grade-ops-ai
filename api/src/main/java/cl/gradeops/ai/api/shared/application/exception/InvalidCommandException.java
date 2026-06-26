package cl.gradeops.ai.api.shared.application.exception;

public class InvalidCommandException extends ApplicationException {
    public InvalidCommandException(String field) {
        super(field + " must not be null");
    }
}
