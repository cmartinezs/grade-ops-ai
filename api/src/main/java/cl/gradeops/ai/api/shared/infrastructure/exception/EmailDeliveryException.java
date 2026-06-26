package cl.gradeops.ai.api.shared.infrastructure.exception;

public class EmailDeliveryException extends InfrastructureException {
    public EmailDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
