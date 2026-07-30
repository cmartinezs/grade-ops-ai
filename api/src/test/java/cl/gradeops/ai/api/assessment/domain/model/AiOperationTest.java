package cl.gradeops.ai.api.assessment.domain.model;

import cl.gradeops.ai.api.shared.domain.exception.DomainInvariantViolationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class AiOperationTest {

    AssessmentId assessmentId() {
        return new AssessmentId(UUID.randomUUID());
    }

    AiOperation createOperation() {
        return AiOperation.create(assessmentId(), AiOperationType.CREATE_INITIAL_REVISION, "teacher-1", "key-1", null);
    }

    @Test
    void shouldStartInPendingStatusWithNoResultRevisionWhenCreated() {
        AiOperation op = createOperation();

        assertThat(op.getId()).isNotNull();
        assertThat(op.getStatus()).isEqualTo(AiOperationStatus.PENDING);
        assertThat(op.getResultRevisionId()).isNull();
        assertThat(op.getOperationType()).isEqualTo(AiOperationType.CREATE_INITIAL_REVISION);
        assertThat(op.getRequestedBy()).isEqualTo("teacher-1");
        assertThat(op.getIdempotencyKey()).isEqualTo("key-1");
        assertThat(op.getExpectedRevisionId()).isNull();
        assertThat(op.getCreatedAt()).isNotNull();
        assertThat(op.getUpdatedAt()).isEqualTo(op.getCreatedAt());
    }

    @Test
    void shouldRejectExpectedRevisionIdForCreateInitialRevision() {
        assertThatThrownBy(() -> AiOperation.create(assessmentId(), AiOperationType.CREATE_INITIAL_REVISION,
                "teacher-1", "key-1", UUID.randomUUID()))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("expectedRevisionId");
    }

    @Test
    void shouldAllowExpectedRevisionIdForRegenerateRevision() {
        UUID expected = UUID.randomUUID();
        AiOperation op = AiOperation.create(assessmentId(), AiOperationType.REGENERATE_REVISION,
                "teacher-1", "key-1", expected);

        assertThat(op.getExpectedRevisionId()).isEqualTo(expected);
    }

    @Test
    void shouldRejectBlankRequestedByWhenCreating() {
        assertThatThrownBy(() -> AiOperation.create(assessmentId(), AiOperationType.CREATE_INITIAL_REVISION,
                " ", "key-1", null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("requestedBy");
    }

    @Test
    void shouldRejectBlankIdempotencyKeyWhenCreating() {
        assertThatThrownBy(() -> AiOperation.create(assessmentId(), AiOperationType.CREATE_INITIAL_REVISION,
                "teacher-1", " ", null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("idempotencyKey");
    }

    @Test
    void shouldTransitionFromPendingToInProgress() {
        AiOperation op = createOperation();

        AiOperation inProgress = op.markInProgress();

        assertThat(inProgress.getStatus()).isEqualTo(AiOperationStatus.IN_PROGRESS);
        assertThat(inProgress.getId()).isEqualTo(op.getId());
        assertThat(op.getStatus()).isEqualTo(AiOperationStatus.PENDING);
    }

    @Test
    void shouldTransitionFromInProgressToSucceededWithResultRevisionId() {
        AiOperation op = createOperation().markInProgress();
        UUID resultRevisionId = UUID.randomUUID();

        AiOperation succeeded = op.markSucceeded(resultRevisionId);

        assertThat(succeeded.getStatus()).isEqualTo(AiOperationStatus.SUCCEEDED);
        assertThat(succeeded.getResultRevisionId()).isEqualTo(resultRevisionId);
    }

    @Test
    void shouldRejectNullResultRevisionIdWhenMarkingSucceeded() {
        AiOperation op = createOperation().markInProgress();

        assertThatThrownBy(() -> op.markSucceeded(null))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("resultRevisionId");
    }

    @Test
    void shouldTransitionFromInProgressToFailedRetryable() {
        AiOperation op = createOperation().markInProgress();

        AiOperation failed = op.markFailedRetryable();

        assertThat(failed.getStatus()).isEqualTo(AiOperationStatus.FAILED_RETRYABLE);
    }

    @Test
    void shouldTransitionFromInProgressToFailedTerminal() {
        AiOperation op = createOperation().markInProgress();

        AiOperation failed = op.markFailedTerminal();

        assertThat(failed.getStatus()).isEqualTo(AiOperationStatus.FAILED_TERMINAL);
    }

    @Test
    void shouldAllowRetryTransitionFromFailedRetryableBackToInProgress() {
        AiOperation op = createOperation().markInProgress().markFailedRetryable();

        AiOperation retried = op.markInProgress();

        assertThat(retried.getStatus()).isEqualTo(AiOperationStatus.IN_PROGRESS);
    }

    @Test
    void shouldAllowRetryTransitionFromInProgressBackToInProgressForAnIndeterminateOrphanedAttempt() {
        // Task 10 / retry: an AiOperation can still be nominally IN_PROGRESS (its only AgentAttempt
        // orphaned past the indeterminate threshold — see GetGenerationStatusHandler) and the
        // Authoring Operation Contract ADR explicitly allows retrying it. This is a deliberate,
        // narrow widening — not a general "IN_PROGRESS is always self-transitionable" rule.
        AiOperation op = createOperation().markInProgress();

        AiOperation retried = op.markInProgress();

        assertThat(retried.getStatus()).isEqualTo(AiOperationStatus.IN_PROGRESS);
        assertThat(retried.getId()).isEqualTo(op.getId());
    }

    @Test
    void shouldRejectTransitionToInProgressFromSucceeded() {
        AiOperation op = createOperation().markInProgress().markSucceeded(UUID.randomUUID());

        assertThatThrownBy(op::markInProgress)
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("SUCCEEDED");
    }

    @Test
    void shouldRejectAnyTransitionFromSucceeded() {
        AiOperation op = createOperation().markInProgress().markSucceeded(UUID.randomUUID());

        assertThatThrownBy(() -> op.markSucceeded(UUID.randomUUID())).isInstanceOf(DomainInvariantViolationException.class);
        assertThatThrownBy(op::markFailedRetryable).isInstanceOf(DomainInvariantViolationException.class);
        assertThatThrownBy(op::markFailedTerminal).isInstanceOf(DomainInvariantViolationException.class);
    }

    @Test
    void shouldRejectAnyTransitionFromFailedTerminal() {
        AiOperation op = createOperation().markInProgress().markFailedTerminal();

        assertThatThrownBy(op::markInProgress).isInstanceOf(DomainInvariantViolationException.class);
        assertThatThrownBy(() -> op.markSucceeded(UUID.randomUUID())).isInstanceOf(DomainInvariantViolationException.class);
        assertThatThrownBy(op::markFailedRetryable).isInstanceOf(DomainInvariantViolationException.class);
    }

    @Test
    void shouldRejectMarkSucceededFromPending() {
        AiOperation op = createOperation();

        assertThatThrownBy(() -> op.markSucceeded(UUID.randomUUID())).isInstanceOf(DomainInvariantViolationException.class);
    }

    @Test
    void shouldRejectMarkFailedRetryableFromPending() {
        AiOperation op = createOperation();

        assertThatThrownBy(op::markFailedRetryable).isInstanceOf(DomainInvariantViolationException.class);
    }

    @Test
    void shouldRestoreAllFieldsVerbatimWhenRestoringFromPersistence() {
        UUID id = UUID.randomUUID();
        AssessmentId assessmentId = assessmentId();
        UUID expectedRevisionId = UUID.randomUUID();
        UUID resultRevisionId = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(120);
        Instant updatedAt = Instant.now().minusSeconds(60);

        AiOperation op = AiOperation.restore(id, assessmentId, AiOperationType.REGENERATE_REVISION, "teacher-1",
                "key-1", expectedRevisionId, AiOperationStatus.SUCCEEDED, resultRevisionId, createdAt, updatedAt);

        assertThat(op.getId()).isEqualTo(id);
        assertThat(op.getAssessmentId()).isEqualTo(assessmentId);
        assertThat(op.getOperationType()).isEqualTo(AiOperationType.REGENERATE_REVISION);
        assertThat(op.getRequestedBy()).isEqualTo("teacher-1");
        assertThat(op.getIdempotencyKey()).isEqualTo("key-1");
        assertThat(op.getExpectedRevisionId()).isEqualTo(expectedRevisionId);
        assertThat(op.getStatus()).isEqualTo(AiOperationStatus.SUCCEEDED);
        assertThat(op.getResultRevisionId()).isEqualTo(resultRevisionId);
        assertThat(op.getCreatedAt()).isEqualTo(createdAt);
        assertThat(op.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void shouldRejectNullIdWhenRestoring() {
        assertThatThrownBy(() -> AiOperation.restore(null, assessmentId(), AiOperationType.CREATE_INITIAL_REVISION,
                "teacher-1", "key-1", null, AiOperationStatus.PENDING, null, Instant.now(), Instant.now()))
                .isInstanceOf(DomainInvariantViolationException.class)
                .hasMessageContaining("id");
    }
}
