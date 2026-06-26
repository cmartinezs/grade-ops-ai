package cl.gradeops.ai.api.auth.infrastructure.exception;

import cl.gradeops.ai.api.shared.infrastructure.exception.InfrastructureException;

public class AuthProviderException extends InfrastructureException {
    public AuthProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
