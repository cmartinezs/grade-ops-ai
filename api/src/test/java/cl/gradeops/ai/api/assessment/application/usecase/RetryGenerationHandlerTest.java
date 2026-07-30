package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AgentClientException;
import cl.gradeops.ai.api.assessment.application.command.RetryGenerationCommand;
import cl.gradeops.ai.api.assessment.application.exception.NoActiveOperationToRetryException;
import cl.gradeops.ai.api.assessment.application.exception.OperationInProgressException;
import cl.gradeops.ai.api.assessment.application.exception.StaleOnCompletionException;
import cl.gradeops.ai.api.assessment.application.port.out.AgentAttemptRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AiOperationRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentBriefRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.RetryGenerationResult;
import cl.gradeops.ai.api.assessment.domain.model.AgentAttempt;
import cl.gradeops.ai.api.assessment.domain.model.AgentAttemptStatus;
import cl.gradeops.ai.api.assessment.domain.model.AiOperation;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationStatus;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationType;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentStatus;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Covers {@link RetryGenerationHandler} with mocked ports — the {@code
 * NO_ACTIVE_OPERATION_TO_RETRY}/{@code OPERATION_IN_PROGRESS} gating, the indeterminate-past-
 * threshold carve-out, and retry's "always respond 202" contract even when the retried dispatch
 * itself fails. Real cross-transaction concurrency (two retries racing on the same {@code
 * AiOperation}) is covered by {@link RetryGenerationHandlerIntegrationTest}.
 */
@ExtendWith(MockitoExtension.class)
class RetryGenerationHandlerTest {

    @Mock AssessmentRepositoryPort assessmentRepository;
    @Mock AssessmentBriefRepositoryPort assessmentBriefRepository;
    @Mock AiOperationRepositoryPort aiOperationRepository;
    @Mock AgentAttemptRepositoryPort agentAttemptRepository;
    @Mock OwnershipVerifier ownershipVerifier;
    @Mock AiOperationCoordinator aiOperationCoordinator;

    RetryGenerationHandler handler;

    final UUID assessmentUuid = UUID.randomUUID();
    final AssessmentId assessmentId = new AssessmentId(assessmentUuid);
    final Assessment assessment = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT, Instant.now());
    final AssessmentBrief brief = AssessmentBrief.create(assessmentId, "goal", "topic", "basic", "90min", "Java");

    @BeforeEach
    void setUp() {
        handler = new RetryGenerationHandler(assessmentRepository, assessmentBriefRepository, aiOperationRepository,
                agentAttemptRepository, ownershipVerifier, aiOperationCoordinator);
    }

    private AiOperation operationWithStatus(AiOperationStatus status) {
        AiOperation op = AiOperation.create(assessmentId, AiOperationType.CREATE_INITIAL_REVISION, "uid-1", "gen-key", null);
        return switch (status) {
            case PENDING -> op;
            case IN_PROGRESS -> op.markInProgress();
            case SUCCEEDED -> op.markInProgress().markSucceeded(UUID.randomUUID());
            case FAILED_RETRYABLE -> op.markInProgress().markFailedRetryable();
            case FAILED_TERMINAL -> op.markInProgress().markFailedTerminal();
        };
    }

    private AgentAttempt dispatchedAttempt(UUID operationId, Instant dispatchedAt) {
        return AgentAttempt.restore(UUID.randomUUID(), operationId, 1, "assessment", null, null, "v1",
                "corr-1", dispatchedAt, null, AgentAttemptStatus.DISPATCHED,
                null, null, null, null, null, null);
    }

    private AgentAttempt failedAttempt(UUID operationId, String failureCode) {
        return AgentAttempt.dispatch(operationId, 1, "assessment", "v1", "corr-1")
                .markFailed(failureCode, null, null, null);
    }

    @Test
    void shouldThrowNotFoundWhenAssessmentDoesNotExist() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(new RetryGenerationCommand(assessmentUuid, "uid-1")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(ownershipVerifier, aiOperationRepository, aiOperationCoordinator);
    }

    @Test
    void shouldRejectWhenTeacherDoesNotOwnAssessment() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        doThrow(new ResourceNotFoundException(assessmentUuid.toString()))
                .when(ownershipVerifier).verify("uid-1", "uid-other", assessmentUuid.toString());

        assertThatThrownBy(() -> handler.execute(new RetryGenerationCommand(assessmentUuid, "uid-other")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(aiOperationRepository, aiOperationCoordinator);
    }

    @Test
    void shouldThrowNoActiveOperationToRetryWhenNoOperationEverExisted() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(aiOperationRepository.findLatestByAssessmentIdAndOperationType(assessmentId, AiOperationType.CREATE_INITIAL_REVISION))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(new RetryGenerationCommand(assessmentUuid, "uid-1")))
                .isInstanceOf(NoActiveOperationToRetryException.class);

        verifyNoInteractions(aiOperationCoordinator);
    }

    @Test
    void shouldThrowNoActiveOperationToRetryWhenLatestOperationSucceeded() {
        AiOperation succeeded = operationWithStatus(AiOperationStatus.SUCCEEDED);
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(aiOperationRepository.findLatestByAssessmentIdAndOperationType(assessmentId, AiOperationType.CREATE_INITIAL_REVISION))
                .thenReturn(Optional.of(succeeded));

        assertThatThrownBy(() -> handler.execute(new RetryGenerationCommand(assessmentUuid, "uid-1")))
                .isInstanceOf(NoActiveOperationToRetryException.class);

        verifyNoInteractions(aiOperationCoordinator);
    }

    @Test
    void shouldThrowNoActiveOperationToRetryWhenLatestOperationFailedTerminal() {
        AiOperation terminal = operationWithStatus(AiOperationStatus.FAILED_TERMINAL);
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(aiOperationRepository.findLatestByAssessmentIdAndOperationType(assessmentId, AiOperationType.CREATE_INITIAL_REVISION))
                .thenReturn(Optional.of(terminal));

        assertThatThrownBy(() -> handler.execute(new RetryGenerationCommand(assessmentUuid, "uid-1")))
                .isInstanceOf(NoActiveOperationToRetryException.class);

        verifyNoInteractions(aiOperationCoordinator);
    }

    @Test
    void shouldThrowOperationInProgressWhenLatestIsInProgressWithinIndeterminateThreshold() {
        AiOperation inProgress = operationWithStatus(AiOperationStatus.IN_PROGRESS);
        AgentAttempt recentAttempt = dispatchedAttempt(inProgress.getId(), Instant.now().minusSeconds(5));
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(aiOperationRepository.findLatestByAssessmentIdAndOperationType(assessmentId, AiOperationType.CREATE_INITIAL_REVISION))
                .thenReturn(Optional.of(inProgress));
        when(agentAttemptRepository.findAllByAiOperationId(inProgress.getId())).thenReturn(List.of(recentAttempt));

        assertThatThrownBy(() -> handler.execute(new RetryGenerationCommand(assessmentUuid, "uid-1")))
                .isInstanceOf(OperationInProgressException.class);

        verifyNoInteractions(aiOperationCoordinator);
    }

    @Test
    void shouldAllowRetryWhenLatestIsInProgressPastIndeterminateThreshold() {
        AiOperation inProgress = operationWithStatus(AiOperationStatus.IN_PROGRESS);
        Instant longAgo = Instant.now().minus(Duration.ofMinutes(10));
        AgentAttempt staleAttempt = dispatchedAttempt(inProgress.getId(), longAgo);
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(aiOperationRepository.findLatestByAssessmentIdAndOperationType(assessmentId, AiOperationType.CREATE_INITIAL_REVISION))
                .thenReturn(Optional.of(inProgress));
        when(agentAttemptRepository.findAllByAiOperationId(inProgress.getId())).thenReturn(List.of(staleAttempt));
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(brief));
        AiOperation retried = inProgress;
        when(aiOperationCoordinator.retryInitialRevision(eq(assessment), eq(inProgress), any(), eq(2)))
                .thenReturn(null);
        when(aiOperationRepository.findById(inProgress.getId())).thenReturn(Optional.of(retried));

        RetryGenerationResult result = handler.execute(new RetryGenerationCommand(assessmentUuid, "uid-1"));

        assertThat(result.id()).isEqualTo(inProgress.getId());
        verify(aiOperationCoordinator).retryInitialRevision(eq(assessment), eq(inProgress), any(), eq(2));
    }

    @Test
    void shouldMarkInProgressAndDispatchNextAttemptWhenFailedRetryable() {
        AiOperation failedRetryable = operationWithStatus(AiOperationStatus.FAILED_RETRYABLE);
        AgentAttempt failed = failedAttempt(failedRetryable.getId(), "AGENT_UNAVAILABLE");
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(aiOperationRepository.findLatestByAssessmentIdAndOperationType(assessmentId, AiOperationType.CREATE_INITIAL_REVISION))
                .thenReturn(Optional.of(failedRetryable));
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(brief));
        AiOperation succeededAfterRetry = failedRetryable.markInProgress().markSucceeded(UUID.randomUUID());
        when(aiOperationCoordinator.retryInitialRevision(eq(assessment), eq(failedRetryable), any(), eq(2)))
                .thenReturn(null);
        when(aiOperationRepository.findById(failedRetryable.getId())).thenReturn(Optional.of(succeededAfterRetry));
        AgentAttempt newAttempt = AgentAttempt.dispatch(failedRetryable.getId(), 2, "assessment", "v1", "corr-2");
        when(agentAttemptRepository.findAllByAiOperationId(failedRetryable.getId()))
                .thenReturn(List.of(failed))
                .thenReturn(List.of(newAttempt, failed));

        RetryGenerationResult result = handler.execute(new RetryGenerationCommand(assessmentUuid, "uid-1"));

        assertThat(result.status()).isEqualTo("SUCCEEDED");
        assertThat(result.retryable()).isFalse();
        verify(aiOperationCoordinator).retryInitialRevision(eq(assessment), eq(failedRetryable), any(), eq(2));
    }

    @Test
    void shouldStillRespondWithOperationSnapshotWhenRetriedDispatchFailsAgain() {
        AiOperation failedRetryable = operationWithStatus(AiOperationStatus.FAILED_RETRYABLE);
        AgentAttempt failed = failedAttempt(failedRetryable.getId(), "AGENT_UNAVAILABLE");
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(aiOperationRepository.findLatestByAssessmentIdAndOperationType(assessmentId, AiOperationType.CREATE_INITIAL_REVISION))
                .thenReturn(Optional.of(failedRetryable));
        when(agentAttemptRepository.findAllByAiOperationId(failedRetryable.getId())).thenReturn(List.of(failed));
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(brief));
        when(aiOperationCoordinator.retryInitialRevision(eq(assessment), eq(failedRetryable), any(), eq(2)))
                .thenThrow(new AgentClientException(AgentClientException.Reason.UNREACHABLE, "down", null));
        when(aiOperationRepository.findById(failedRetryable.getId())).thenReturn(Optional.of(failedRetryable));

        RetryGenerationResult result = handler.execute(new RetryGenerationCommand(assessmentUuid, "uid-1"));

        assertThat(result.status()).isEqualTo("FAILED_RETRYABLE");
    }

    @Test
    void shouldStillRespondWithOperationSnapshotWhenRetriedDispatchLosesCasRace() {
        AiOperation failedRetryable = operationWithStatus(AiOperationStatus.FAILED_RETRYABLE);
        AgentAttempt failed = failedAttempt(failedRetryable.getId(), "AGENT_UNAVAILABLE");
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(aiOperationRepository.findLatestByAssessmentIdAndOperationType(assessmentId, AiOperationType.CREATE_INITIAL_REVISION))
                .thenReturn(Optional.of(failedRetryable));
        when(agentAttemptRepository.findAllByAiOperationId(failedRetryable.getId())).thenReturn(List.of(failed));
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(brief));
        when(aiOperationCoordinator.retryInitialRevision(eq(assessment), eq(failedRetryable), any(), eq(2)))
                .thenThrow(new StaleOnCompletionException(assessmentUuid.toString()));
        AiOperation terminalAfterStaleCompletion = failedRetryable.markInProgress().markFailedTerminal();
        when(aiOperationRepository.findById(failedRetryable.getId())).thenReturn(Optional.of(terminalAfterStaleCompletion));

        RetryGenerationResult result = handler.execute(new RetryGenerationCommand(assessmentUuid, "uid-1"));

        assertThat(result.status()).isEqualTo("FAILED_TERMINAL");
    }
}
