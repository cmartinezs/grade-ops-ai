package cl.gradeops.ai.api.assessment.application.exception;

import cl.gradeops.ai.api.shared.application.exception.ApplicationException;

import java.util.UUID;

/** Initial generation requested twice — a current revision already exists. */
public class AlreadyGeneratedException extends ApplicationException {

    private final UUID currentRevisionId;

    public AlreadyGeneratedException(String assessmentId, UUID currentRevisionId) {
        super("Assessment " + assessmentId + " already has a current revision: " + currentRevisionId);
        this.currentRevisionId = currentRevisionId;
    }

    public UUID currentRevisionId() {
        return currentRevisionId;
    }
}
