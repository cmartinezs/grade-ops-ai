package cl.gradeops.ai.api.assessment.application.exception;

import cl.gradeops.ai.api.shared.application.exception.ApplicationException;

/**
 * The agent call succeeded, but by the time of the Phase 2 CAS write {@code
 * Assessment.currentRevisionId} had already moved (a different, faster operation won). The
 * structured result is still persisted on the {@code AgentAttempt} for evidence — this
 * exception only signals that no new {@code AssessmentRevision} was created from it.
 */
public class StaleOnCompletionException extends ApplicationException {
    public StaleOnCompletionException(String assessmentId) {
        super("Assessment " + assessmentId + "'s current revision changed before this operation's result could be applied");
    }
}
