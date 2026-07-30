package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.assessment.application.command.GetGenerationStatusCommand;
import cl.gradeops.ai.api.assessment.application.port.out.AgentAttemptRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AiOperationRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.GetGenerationStatusResult;
import cl.gradeops.ai.api.assessment.domain.model.AgentAttempt;
import cl.gradeops.ai.api.assessment.domain.model.AgentAttemptStatus;
import cl.gradeops.ai.api.assessment.domain.model.AiOperation;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationStatus;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationType;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
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
import static org.mockito.Mockito.*;

/**
 * Covers {@link GetGenerationStatusHandler}'s full read-time status taxonomy: {@code
 * currentRevisionId} short-circuit, {@code NOT_STARTED}, every {@code AiOperation} status
 * (including the {@code FAILED_TERMINAL} taxonomy-gap fill this session made), and the
 * {@code INDETERMINATE} orphaned-attempt classification computed at read time from {@code
 * agentclient.read-timeout × 5}.
 */
@ExtendWith(MockitoExtension.class)
class GetGenerationStatusHandlerTest {

    @Mock AssessmentRepositoryPort assessmentRepository;
    @Mock AiOperationRepositoryPort aiOperationRepository;
    @Mock AgentAttemptRepositoryPort agentAttemptRepository;
    @Mock OwnershipVerifier ownershipVerifier;

    GetGenerationStatusHandler handler;

    final UUID assessmentUuid = UUID.randomUUID();
    final AssessmentId assessmentId = new AssessmentId(assessmentUuid);

    @BeforeEach
    void setUp() {
        handler = new GetGenerationStatusHandler(assessmentRepository, aiOperationRepository,
                agentAttemptRepository, ownershipVerifier);
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

        assertThatThrownBy(() -> handler.execute(new GetGenerationStatusCommand(assessmentUuid, "uid-1")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(aiOperationRepository);
    }

    @Test
    void shouldRejectWhenTeacherDoesNotOwnAssessment() {
        Assessment assessment = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT, Instant.now());
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        doThrow(new ResourceNotFoundException(assessmentUuid.toString()))
                .when(ownershipVerifier).verify("uid-1", "uid-other", assessmentUuid.toString());

        assertThatThrownBy(() -> handler.execute(new GetGenerationStatusCommand(assessmentUuid, "uid-other")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(aiOperationRepository);
    }

    @Test
    void shouldReportSucceededFromCurrentRevisionIdWithoutConsultingAiOperationEvenForAHumanEditOnlyAssessment() {
        UUID currentRevisionId = UUID.randomUUID();
        Assessment assessment = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT, Instant.now(),
                currentRevisionId, 0);
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));

        GetGenerationStatusResult result = handler.execute(new GetGenerationStatusCommand(assessmentUuid, "uid-1"));

        assertThat(result.status()).isEqualTo("SUCCEEDED");
        assertThat(result.currentRevisionId()).isEqualTo(currentRevisionId);
        assertThat(result.retryable()).isFalse();
        verifyNoInteractions(aiOperationRepository);
    }

    @Test
    void shouldReportNotStartedWhenNoRevisionAndNoOperationExistsYet() {
        Assessment assessment = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT, Instant.now());
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(aiOperationRepository.findLatestByAssessmentIdAndOperationType(assessmentId, AiOperationType.CREATE_INITIAL_REVISION))
                .thenReturn(Optional.empty());

        GetGenerationStatusResult result = handler.execute(new GetGenerationStatusCommand(assessmentUuid, "uid-1"));

        assertThat(result.status()).isEqualTo("NOT_STARTED");
        assertThat(result.currentRevisionId()).isNull();
    }

    @Test
    void shouldReportFailedRetryableWithFailureCodeFromLatestAttempt() {
        Assessment assessment = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT, Instant.now());
        AiOperation op = AiOperation.create(assessmentId, AiOperationType.CREATE_INITIAL_REVISION, "uid-1", "key", null)
                .markInProgress().markFailedRetryable();
        AgentAttempt failed = failedAttempt(op.getId(), "AGENT_UNAVAILABLE");
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(aiOperationRepository.findLatestByAssessmentIdAndOperationType(assessmentId, AiOperationType.CREATE_INITIAL_REVISION))
                .thenReturn(Optional.of(op));
        when(agentAttemptRepository.findAllByAiOperationId(op.getId())).thenReturn(List.of(failed));

        GetGenerationStatusResult result = handler.execute(new GetGenerationStatusCommand(assessmentUuid, "uid-1"));

        assertThat(result.status()).isEqualTo("FAILED_RETRYABLE");
        assertThat(result.failureCode()).isEqualTo("AGENT_UNAVAILABLE");
        assertThat(result.retryable()).isTrue();
    }

    @Test
    void shouldReportFailedTerminalAsNonRetryable() {
        Assessment assessment = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT, Instant.now());
        AiOperation op = AiOperation.create(assessmentId, AiOperationType.CREATE_INITIAL_REVISION, "uid-1", "key", null)
                .markInProgress().markFailedTerminal();
        AgentAttempt failed = failedAttempt(op.getId(), "INVALID_COMMAND");
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(aiOperationRepository.findLatestByAssessmentIdAndOperationType(assessmentId, AiOperationType.CREATE_INITIAL_REVISION))
                .thenReturn(Optional.of(op));
        when(agentAttemptRepository.findAllByAiOperationId(op.getId())).thenReturn(List.of(failed));

        GetGenerationStatusResult result = handler.execute(new GetGenerationStatusCommand(assessmentUuid, "uid-1"));

        assertThat(result.status()).isEqualTo("FAILED_TERMINAL");
        assertThat(result.failureCode()).isEqualTo("INVALID_COMMAND");
        assertThat(result.retryable()).isFalse();
    }

    @Test
    void shouldReportInProgressWhenLatestAttemptIsRecentlyDispatched() {
        Assessment assessment = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT, Instant.now());
        AiOperation op = AiOperation.create(assessmentId, AiOperationType.CREATE_INITIAL_REVISION, "uid-1", "key", null)
                .markInProgress();
        AgentAttempt recent = dispatchedAttempt(op.getId(), Instant.now().minusSeconds(5));
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(aiOperationRepository.findLatestByAssessmentIdAndOperationType(assessmentId, AiOperationType.CREATE_INITIAL_REVISION))
                .thenReturn(Optional.of(op));
        when(agentAttemptRepository.findAllByAiOperationId(op.getId())).thenReturn(List.of(recent));

        GetGenerationStatusResult result = handler.execute(new GetGenerationStatusCommand(assessmentUuid, "uid-1"));

        assertThat(result.status()).isEqualTo("IN_PROGRESS");
        assertThat(result.retryable()).isFalse();
    }

    @Test
    void shouldReportIndeterminateWhenDispatchedAttemptIsPastFiveTimesTheReadTimeout() {
        Assessment assessment = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT, Instant.now());
        AiOperation op = AiOperation.create(assessmentId, AiOperationType.CREATE_INITIAL_REVISION, "uid-1", "key", null)
                .markInProgress();
        Instant longAgo = Instant.now().minus(Duration.ofMinutes(10));
        AgentAttempt stale = dispatchedAttempt(op.getId(), longAgo);
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(aiOperationRepository.findLatestByAssessmentIdAndOperationType(assessmentId, AiOperationType.CREATE_INITIAL_REVISION))
                .thenReturn(Optional.of(op));
        when(agentAttemptRepository.findAllByAiOperationId(op.getId())).thenReturn(List.of(stale));

        GetGenerationStatusResult result = handler.execute(new GetGenerationStatusCommand(assessmentUuid, "uid-1"));

        assertThat(result.status()).isEqualTo("INDETERMINATE");
        assertThat(result.retryable()).isTrue();
        assertThat(result.failureCode()).isNull();
    }

    @Test
    void shouldReportSucceededFromTheAiOperationWhenReadThroughTheOperationItself() {
        Assessment assessment = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT, Instant.now());
        UUID resultRevisionId = UUID.randomUUID();
        AiOperation op = AiOperation.create(assessmentId, AiOperationType.CREATE_INITIAL_REVISION, "uid-1", "key", null)
                .markInProgress().markSucceeded(resultRevisionId);
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(aiOperationRepository.findLatestByAssessmentIdAndOperationType(assessmentId, AiOperationType.CREATE_INITIAL_REVISION))
                .thenReturn(Optional.of(op));

        GetGenerationStatusResult result = handler.execute(new GetGenerationStatusCommand(assessmentUuid, "uid-1"));

        assertThat(result.status()).isEqualTo("SUCCEEDED");
        assertThat(result.currentRevisionId()).isEqualTo(resultRevisionId);
    }
}
