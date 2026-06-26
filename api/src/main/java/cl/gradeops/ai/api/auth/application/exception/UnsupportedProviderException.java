package cl.gradeops.ai.api.auth.application.exception;

import cl.gradeops.ai.api.shared.application.exception.ApplicationException;

public class UnsupportedProviderException extends ApplicationException {
    public UnsupportedProviderException(String message) {
        super(message);
    }
}
