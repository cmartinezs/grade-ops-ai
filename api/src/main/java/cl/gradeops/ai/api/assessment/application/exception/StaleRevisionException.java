package cl.gradeops.ai.api.assessment.application.exception;

import cl.gradeops.ai.api.shared.application.exception.ApplicationException;

/**
 * {@code expectedRevisionId} sent by the client no longer matches {@code
 * Assessment.currentRevisionId} — either detected by the pre-dispatch/pre-write check, or by a
 * lost {@code lockVersion} CAS race between two concurrent writers reading the same expected
 * value. Both surface identically here since, unlike the AI-dispatch paths (which have a real
 * async gap and so distinguish this from {@link StaleOnCompletionException}), a human edit's
 * check and write happen in the same transaction with no external call in between.
 */
public class StaleRevisionException extends ApplicationException {
    public StaleRevisionException(String assessmentId) {
        super("Assessment " + assessmentId + "'s current revision changed since it was last read — reload and retry");
    }
}
