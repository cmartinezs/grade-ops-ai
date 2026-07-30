package cl.gradeops.ai.api.assessment.domain.model;

import cl.gradeops.ai.api.shared.domain.exception.DomainInvariantViolationException;
import cl.gradeops.ai.api.shared.domain.model.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

/**
 * The durable functional intent behind an AI dispatch (generate/regenerate) — survives across
 * every user-initiated retry. {@link #markInProgress()}/{@link #markSucceeded(UUID)}/
 * {@link #markFailedRetryable()}/{@link #markFailedTerminal()} enforce the valid state-transition
 * graph; e.g. a {@code SUCCEEDED} or {@code FAILED_TERMINAL} operation accepts no further transition.
 */
public class AiOperation extends AggregateRoot<UUID> {

    private UUID id;
    private AssessmentId assessmentId;
    private AiOperationType operationType;
    private String requestedBy;
    private String idempotencyKey;
    private UUID expectedRevisionId;
    private AiOperationStatus status;
    private UUID resultRevisionId;
    private Instant createdAt;
    private Instant updatedAt;

    private AiOperation() {}

    public static AiOperation create(AssessmentId assessmentId, AiOperationType operationType, String requestedBy,
                                      String idempotencyKey, UUID expectedRevisionId) {
        if (assessmentId == null) throw new DomainInvariantViolationException("assessmentId must not be null");
        if (operationType == null) throw new DomainInvariantViolationException("operationType must not be null");
        if (requestedBy == null || requestedBy.isBlank())
            throw new DomainInvariantViolationException("requestedBy must not be blank");
        if (idempotencyKey == null || idempotencyKey.isBlank())
            throw new DomainInvariantViolationException("idempotencyKey must not be blank");
        if (operationType == AiOperationType.CREATE_INITIAL_REVISION && expectedRevisionId != null)
            throw new DomainInvariantViolationException("expectedRevisionId must be null for CREATE_INITIAL_REVISION");
        AiOperation op = new AiOperation();
        op.id = UUID.randomUUID();
        op.assessmentId = assessmentId;
        op.operationType = operationType;
        op.requestedBy = requestedBy;
        op.idempotencyKey = idempotencyKey;
        op.expectedRevisionId = expectedRevisionId;
        op.status = AiOperationStatus.PENDING;
        op.resultRevisionId = null;
        op.createdAt = Instant.now();
        op.updatedAt = op.createdAt;
        return op;
    }

    public static AiOperation restore(UUID id, AssessmentId assessmentId, AiOperationType operationType, String requestedBy,
                                       String idempotencyKey, UUID expectedRevisionId, AiOperationStatus status,
                                       UUID resultRevisionId, Instant createdAt, Instant updatedAt) {
        if (id == null) throw new DomainInvariantViolationException("id must not be null");
        if (assessmentId == null) throw new DomainInvariantViolationException("assessmentId must not be null");
        if (operationType == null) throw new DomainInvariantViolationException("operationType must not be null");
        if (requestedBy == null || requestedBy.isBlank())
            throw new DomainInvariantViolationException("requestedBy must not be blank");
        if (idempotencyKey == null || idempotencyKey.isBlank())
            throw new DomainInvariantViolationException("idempotencyKey must not be blank");
        if (status == null) throw new DomainInvariantViolationException("status must not be null");
        if (createdAt == null) throw new DomainInvariantViolationException("createdAt must not be null");
        if (updatedAt == null) throw new DomainInvariantViolationException("updatedAt must not be null");
        AiOperation op = new AiOperation();
        op.id = id;
        op.assessmentId = assessmentId;
        op.operationType = operationType;
        op.requestedBy = requestedBy;
        op.idempotencyKey = idempotencyKey;
        op.expectedRevisionId = expectedRevisionId;
        op.status = status;
        op.resultRevisionId = resultRevisionId;
        op.createdAt = createdAt;
        op.updatedAt = updatedAt;
        return op;
    }

    /**
     * PENDING/FAILED_RETRYABLE → IN_PROGRESS: initial dispatch, or a user-initiated retry.
     * IN_PROGRESS → IN_PROGRESS is also allowed — narrowly, only for retrying an operation whose
     * sole {@code AgentAttempt} has gone orphaned/indeterminate (Authoring Operation Contract ADR
     * § "Retry a failed generation": "If the latest is IN_PROGRESS past the indeterminate
     * threshold → allowed"). The indeterminate-threshold check itself is the read-side handler's
     * job, not this domain method's — this transition only states that the state machine permits
     * it when the caller has already established that precondition.
     */
    public AiOperation markInProgress() {
        if (status != AiOperationStatus.PENDING && status != AiOperationStatus.FAILED_RETRYABLE
                && status != AiOperationStatus.IN_PROGRESS) {
            throw new DomainInvariantViolationException("cannot transition to IN_PROGRESS from " + status);
        }
        return transitionTo(AiOperationStatus.IN_PROGRESS, resultRevisionId);
    }

    /** IN_PROGRESS → SUCCEEDED, recording the revision this operation produced. */
    public AiOperation markSucceeded(UUID resultRevisionId) {
        if (status != AiOperationStatus.IN_PROGRESS) {
            throw new DomainInvariantViolationException("cannot transition to SUCCEEDED from " + status);
        }
        if (resultRevisionId == null) throw new DomainInvariantViolationException("resultRevisionId must not be null");
        return transitionTo(AiOperationStatus.SUCCEEDED, resultRevisionId);
    }

    /** IN_PROGRESS → FAILED_RETRYABLE, e.g. a transient agent/transport failure. */
    public AiOperation markFailedRetryable() {
        if (status != AiOperationStatus.IN_PROGRESS) {
            throw new DomainInvariantViolationException("cannot transition to FAILED_RETRYABLE from " + status);
        }
        return transitionTo(AiOperationStatus.FAILED_RETRYABLE, resultRevisionId);
    }

    /** IN_PROGRESS → FAILED_TERMINAL, e.g. a validation-class failure or a stale-on-completion CAS loss. */
    public AiOperation markFailedTerminal() {
        if (status != AiOperationStatus.IN_PROGRESS) {
            throw new DomainInvariantViolationException("cannot transition to FAILED_TERMINAL from " + status);
        }
        return transitionTo(AiOperationStatus.FAILED_TERMINAL, resultRevisionId);
    }

    private AiOperation transitionTo(AiOperationStatus newStatus, UUID resultRevisionId) {
        AiOperation op = new AiOperation();
        op.id = id;
        op.assessmentId = assessmentId;
        op.operationType = operationType;
        op.requestedBy = requestedBy;
        op.idempotencyKey = idempotencyKey;
        op.expectedRevisionId = expectedRevisionId;
        op.status = newStatus;
        op.resultRevisionId = resultRevisionId;
        op.createdAt = createdAt;
        op.updatedAt = Instant.now();
        return op;
    }

    @Override protected UUID id()                  { return id; }
    public UUID getId()                            { return id; }
    public AssessmentId getAssessmentId()          { return assessmentId; }
    public AiOperationType getOperationType()      { return operationType; }
    public String getRequestedBy()                 { return requestedBy; }
    public String getIdempotencyKey()               { return idempotencyKey; }
    public UUID getExpectedRevisionId()            { return expectedRevisionId; }
    public AiOperationStatus getStatus()            { return status; }
    public UUID getResultRevisionId()              { return resultRevisionId; }
    public Instant getCreatedAt()                   { return createdAt; }
    public Instant getUpdatedAt()                   { return updatedAt; }
}
