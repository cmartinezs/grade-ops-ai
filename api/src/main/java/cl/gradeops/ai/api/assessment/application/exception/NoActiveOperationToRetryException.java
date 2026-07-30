package cl.gradeops.ai.api.assessment.application.exception;

import cl.gradeops.ai.api.shared.application.exception.ApplicationException;

/**
 * {@code POST .../draft/retry} was called but no {@code AiOperation} exists for this assessment,
 * or the latest one is not in a retryable state (SUCCEEDED — a revision already exists; or
 * FAILED_TERMINAL — a non-retryable failure). Not retryable by the client per the Authoring
 * Operation Contract's typed error table.
 */
public class NoActiveOperationToRetryException extends ApplicationException {
    public NoActiveOperationToRetryException(String assessmentId) {
        super("Assessment " + assessmentId + " has no retryable generation operation");
    }
}
