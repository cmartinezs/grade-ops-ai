package cl.gradeops.ai.api.assessment.application.exception;

import cl.gradeops.ai.api.shared.application.exception.ApplicationException;

/** Thrown when regeneration (US-012) is requested before any draft has ever been generated. */
public class NoPriorDraftException extends ApplicationException {
    public NoPriorDraftException(String assessmentId) {
        super("No draft exists yet for assessment " + assessmentId + " — generate one before regenerating");
    }
}
