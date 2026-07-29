package cl.gradeops.ai.api.shared.application.idempotency;

import cl.gradeops.ai.api.shared.domain.exception.DomainInvariantViolationException;

import java.util.UUID;

/**
 * Matches the two scopes in the Idempotency and Concurrency Strategy ADR: creation-type
 * operations key on {@code (teacherUid, operationType, idempotencyKey)}, assessment-scoped
 * mutations key on {@code (assessmentId, operationType, idempotencyKey)}.
 */
public record IdempotencyScope(IdempotencyScopeType type, String teacherUid, UUID assessmentId) {

    public IdempotencyScope {
        if (type == null) throw new DomainInvariantViolationException("scope type must not be null");
        if (type == IdempotencyScopeType.TEACHER) {
            if (teacherUid == null || teacherUid.isBlank())
                throw new DomainInvariantViolationException("teacherUid must not be blank for TEACHER scope");
            if (assessmentId != null)
                throw new DomainInvariantViolationException("assessmentId must be null for TEACHER scope");
        } else {
            if (assessmentId == null)
                throw new DomainInvariantViolationException("assessmentId must not be null for ASSESSMENT scope");
            if (teacherUid != null)
                throw new DomainInvariantViolationException("teacherUid must be null for ASSESSMENT scope");
        }
    }

    public static IdempotencyScope teacher(String teacherUid) {
        return new IdempotencyScope(IdempotencyScopeType.TEACHER, teacherUid, null);
    }

    public static IdempotencyScope assessment(UUID assessmentId) {
        return new IdempotencyScope(IdempotencyScopeType.ASSESSMENT, null, assessmentId);
    }
}
