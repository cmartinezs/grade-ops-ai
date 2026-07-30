package cl.gradeops.ai.api.assessment.application.exception;

import cl.gradeops.ai.api.shared.application.exception.ApplicationException;

/**
 * {@code POST .../draft/retry} was called while the latest {@code AiOperation} is still
 * PENDING/IN_PROGRESS and within the indeterminate threshold — a real attempt may still be
 * running, so a second dispatch is refused rather than risking a double-charge. Retryable by the
 * client (poll {@code generation-status}, then retry) once the attempt either completes or ages
 * past the indeterminate threshold.
 */
public class OperationInProgressException extends ApplicationException {
    public OperationInProgressException(String assessmentId) {
        super("Assessment " + assessmentId + " already has a generation operation in progress");
    }
}
