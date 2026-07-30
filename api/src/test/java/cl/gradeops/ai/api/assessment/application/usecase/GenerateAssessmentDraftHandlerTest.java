package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AgentClientException;
import cl.gradeops.ai.api.agentclient.AssessmentCommand;
import cl.gradeops.ai.api.assessment.application.command.GenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.exception.AlreadyGeneratedException;
import cl.gradeops.ai.api.assessment.application.exception.StaleOnCompletionException;
import cl.gradeops.ai.api.assessment.application.port.out.AgentAttemptRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AiOperationRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentBriefRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRevisionRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftOutcome;
import cl.gradeops.ai.api.assessment.domain.model.AgentAttempt;
import cl.gradeops.ai.api.assessment.domain.model.AiOperation;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationType;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentStatus;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyGuard;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyRecord;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyScope;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
 * Covers only this handler's own responsibilities — ownership, idempotency (Task 06), the
 * {@code ALREADY_GENERATED} precondition, delegation to {@link AiOperationCoordinator}, and (A3
 * Contract Correction § Correction 2) translating a dispatch failure into a {@code 202}
 * {@code OperationAccepted} outcome instead of letting it propagate — with the coordinator
 * mocked. The coordinator's own three-phase behavior is covered by {@link
 * AiOperationCoordinatorTest} instead.
 */
@ExtendWith(MockitoExtension.class)
class GenerateAssessmentDraftHandlerTest {

    @Mock AssessmentRepositoryPort assessmentRepository;
    @Mock AssessmentBriefRepositoryPort assessmentBriefRepository;
    @Mock AssessmentRevisionRepositoryPort assessmentRevisionRepository;
    @Mock AiOperationRepositoryPort aiOperationRepository;
    @Mock AgentAttemptRepositoryPort agentAttemptRepository;
    @Mock OwnershipVerifier ownershipVerifier;
    @Mock IdempotencyGuard idempotencyGuard;
    @Mock AiOperationCoordinator aiOperationCoordinator;

    GenerateAssessmentDraftHandler handler;

    final UUID assessmentUuid = UUID.randomUUID();
    final AssessmentId assessmentId = new AssessmentId(assessmentUuid);
    final Assessment assessment = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT, Instant.now());
    final AssessmentBrief brief = AssessmentBrief.create(assessmentId, "goal", "topic", "basic", "90min", "Java");

    @BeforeEach
    void setUp() {
        handler = new GenerateAssessmentDraftHandler(assessmentRepository, assessmentBriefRepository,
                assessmentRevisionRepository, aiOperationRepository, agentAttemptRepository,
                ownershipVerifier, idempotencyGuard, aiOperationCoordinator);
    }

    private static AssessmentRevision revision(AssessmentId assessmentId, String title, int versionNumber) {
        return AssessmentRevision.generateFromAi(assessmentId, title, "Context", "Instructions",
                List.of("obj"), List.of("del"), List.of("con"), "uid-1", UUID.randomUUID());
    }

    private static AiOperation failedOperation(cl.gradeops.ai.api.assessment.domain.model.AiOperationStatus status) {
        AiOperation inProgress = AiOperation.create(new AssessmentId(UUID.randomUUID()), AiOperationType.CREATE_INITIAL_REVISION,
                        "uid-1", "key-1", null)
                .markInProgress();
        return status == cl.gradeops.ai.api.assessment.domain.model.AiOperationStatus.FAILED_RETRYABLE
                ? inProgress.markFailedRetryable()
                : inProgress.markFailedTerminal();
    }

    @Test
    void shouldCheckIdempotencyThenDelegateToCoordinatorThenRecordOnSuccess() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(idempotencyGuard.check(any(), eq("CREATE_INITIAL_REVISION"), eq("key-1"), any())).thenReturn(Optional.empty());
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(brief));
        AssessmentRevision revision = revision(assessmentId, "Title", 1);
        when(aiOperationCoordinator.createInitialRevision(eq(assessment), any(), eq("uid-1"), eq("key-1")))
                .thenReturn(revision);

        GenerateAssessmentDraftOutcome outcome = handler.execute(
                new GenerateAssessmentDraftCommand(assessmentUuid, "uid-1", "key-1"));

        assertThat(outcome).isInstanceOf(GenerateAssessmentDraftOutcome.RevisionCreated.class);
        var result = ((GenerateAssessmentDraftOutcome.RevisionCreated) outcome).revision();
        assertThat(result.draftId()).isEqualTo(revision.getId());
        assertThat(result.title()).isEqualTo("Title");
        assertThat(result.versionNumber()).isEqualTo(1);

        verify(ownershipVerifier).verify("uid-1", "uid-1", assessmentUuid.toString());

        ArgumentCaptor<IdempotencyScope> scopeCaptor = ArgumentCaptor.forClass(IdempotencyScope.class);
        verify(idempotencyGuard).check(scopeCaptor.capture(), eq("CREATE_INITIAL_REVISION"), eq("key-1"), any());
        assertThat(scopeCaptor.getValue()).isEqualTo(IdempotencyScope.assessment(assessmentUuid));

        ArgumentCaptor<AssessmentCommand> commandCaptor = ArgumentCaptor.forClass(AssessmentCommand.class);
        verify(aiOperationCoordinator).createInitialRevision(eq(assessment), commandCaptor.capture(), eq("uid-1"), eq("key-1"));
        assertThat(commandCaptor.getValue().learningGoal()).isEqualTo("goal");
        assertThat(commandCaptor.getValue().topic()).isEqualTo("topic");

        verify(idempotencyGuard).record(eq(scopeCaptor.getValue()), eq("CREATE_INITIAL_REVISION"), eq("key-1"),
                any(), eq(revision.getId().toString()), eq(201));
    }

    @Test
    void shouldReplayPriorRecordWithoutTouchingBriefOrCoordinatorWhenIdempotencyKeyAlreadyUsed() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        AssessmentRevision priorRevision = revision(assessmentId, "Prior Title", 1);
        IdempotencyRecord priorRecord = IdempotencyRecord.create(IdempotencyScope.assessment(assessmentUuid),
                "CREATE_INITIAL_REVISION", "key-1", "hash", priorRevision.getId().toString(), 201);
        when(idempotencyGuard.check(any(), eq("CREATE_INITIAL_REVISION"), eq("key-1"), any()))
                .thenReturn(Optional.of(priorRecord));
        when(assessmentRevisionRepository.findById(priorRevision.getId())).thenReturn(Optional.of(priorRevision));

        GenerateAssessmentDraftOutcome outcome = handler.execute(
                new GenerateAssessmentDraftCommand(assessmentUuid, "uid-1", "key-1"));

        var result = ((GenerateAssessmentDraftOutcome.RevisionCreated) outcome).revision();
        assertThat(result.title()).isEqualTo("Prior Title");
        verifyNoInteractions(assessmentBriefRepository, aiOperationCoordinator);
    }

    @Test
    void shouldReturnOperationAcceptedWhenTheCoordinatorThrowsAgentClientException() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(idempotencyGuard.check(any(), eq("CREATE_INITIAL_REVISION"), eq("key-1"), any())).thenReturn(Optional.empty());
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(brief));
        AgentClientException agentEx = new AgentClientException(
                AgentClientException.Reason.UNREACHABLE, "unreachable", new RuntimeException());
        when(aiOperationCoordinator.createInitialRevision(eq(assessment), any(), eq("uid-1"), eq("key-1")))
                .thenThrow(agentEx);
        AiOperation failedOp = failedOperation(cl.gradeops.ai.api.assessment.domain.model.AiOperationStatus.FAILED_RETRYABLE);
        when(aiOperationRepository.findLatestByAssessmentIdAndOperationType(assessmentId, AiOperationType.CREATE_INITIAL_REVISION))
                .thenReturn(Optional.of(failedOp));
        AgentAttempt failedAttempt = AgentAttempt.dispatch(failedOp.getId(), 1, "assessment", "v1", "corr-1")
                .markFailed("AGENT_UNAVAILABLE", null, null, null);
        when(agentAttemptRepository.findAllByAiOperationId(failedOp.getId())).thenReturn(List.of(failedAttempt));

        GenerateAssessmentDraftOutcome outcome = handler.execute(
                new GenerateAssessmentDraftCommand(assessmentUuid, "uid-1", "key-1"));

        assertThat(outcome).isInstanceOf(GenerateAssessmentDraftOutcome.OperationAccepted.class);
        var operation = ((GenerateAssessmentDraftOutcome.OperationAccepted) outcome).operation();
        assertThat(operation.id()).isEqualTo(failedOp.getId());
        assertThat(operation.status()).isEqualTo("FAILED_RETRYABLE");
        assertThat(operation.failureCode()).isEqualTo("AGENT_UNAVAILABLE");
        assertThat(operation.retryable()).isTrue();

        verify(idempotencyGuard).record(any(), eq("CREATE_INITIAL_REVISION"), eq("key-1"),
                any(), eq(failedOp.getId().toString()), eq(202));
    }

    @Test
    void shouldReturnOperationAcceptedWhenTheCoordinatorThrowsStaleOnCompletion() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(idempotencyGuard.check(any(), eq("CREATE_INITIAL_REVISION"), eq("key-1"), any())).thenReturn(Optional.empty());
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(brief));
        when(aiOperationCoordinator.createInitialRevision(eq(assessment), any(), eq("uid-1"), eq("key-1")))
                .thenThrow(new StaleOnCompletionException(assessmentUuid.toString()));
        AiOperation failedOp = failedOperation(cl.gradeops.ai.api.assessment.domain.model.AiOperationStatus.FAILED_TERMINAL);
        when(aiOperationRepository.findLatestByAssessmentIdAndOperationType(assessmentId, AiOperationType.CREATE_INITIAL_REVISION))
                .thenReturn(Optional.of(failedOp));
        AgentAttempt failedAttempt = AgentAttempt.dispatch(failedOp.getId(), 1, "assessment", "v1", "corr-1")
                .markFailed("STALE_ON_COMPLETION", "gemini", "gemini-2.0-flash", null);
        when(agentAttemptRepository.findAllByAiOperationId(failedOp.getId())).thenReturn(List.of(failedAttempt));

        GenerateAssessmentDraftOutcome outcome = handler.execute(
                new GenerateAssessmentDraftCommand(assessmentUuid, "uid-1", "key-1"));

        var operation = ((GenerateAssessmentDraftOutcome.OperationAccepted) outcome).operation();
        assertThat(operation.status()).isEqualTo("FAILED_TERMINAL");
        assertThat(operation.failureCode()).isEqualTo("STALE_ON_COMPLETION");
        assertThat(operation.retryable()).isFalse();
    }

    @Test
    void shouldReplayA202RecordAsOperationAcceptedWithoutTouchingBriefOrCoordinator() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        AiOperation failedOp = failedOperation(cl.gradeops.ai.api.assessment.domain.model.AiOperationStatus.FAILED_RETRYABLE);
        IdempotencyRecord priorRecord = IdempotencyRecord.create(IdempotencyScope.assessment(assessmentUuid),
                "CREATE_INITIAL_REVISION", "key-1", "hash", failedOp.getId().toString(), 202);
        when(idempotencyGuard.check(any(), eq("CREATE_INITIAL_REVISION"), eq("key-1"), any()))
                .thenReturn(Optional.of(priorRecord));
        when(aiOperationRepository.findById(failedOp.getId())).thenReturn(Optional.of(failedOp));
        AgentAttempt failedAttempt = AgentAttempt.dispatch(failedOp.getId(), 1, "assessment", "v1", "corr-1")
                .markFailed("AGENT_UNAVAILABLE", null, null, null);
        when(agentAttemptRepository.findAllByAiOperationId(failedOp.getId())).thenReturn(List.of(failedAttempt));

        GenerateAssessmentDraftOutcome outcome = handler.execute(
                new GenerateAssessmentDraftCommand(assessmentUuid, "uid-1", "key-1"));

        assertThat(outcome).isInstanceOf(GenerateAssessmentDraftOutcome.OperationAccepted.class);
        var operation = ((GenerateAssessmentDraftOutcome.OperationAccepted) outcome).operation();
        assertThat(operation.id()).isEqualTo(failedOp.getId());
        verifyNoInteractions(assessmentBriefRepository, aiOperationCoordinator);
    }

    @Test
    void shouldThrowAlreadyGeneratedWhenCurrentRevisionAlreadyExists() {
        UUID existingRevisionId = UUID.randomUUID();
        Assessment alreadyGenerated = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT,
                Instant.now(), existingRevisionId, 1);
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(alreadyGenerated));
        when(idempotencyGuard.check(any(), eq("CREATE_INITIAL_REVISION"), eq("key-1"), any())).thenReturn(Optional.empty());
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(brief));

        assertThatThrownBy(() -> handler.execute(new GenerateAssessmentDraftCommand(assessmentUuid, "uid-1", "key-1")))
                .isInstanceOf(AlreadyGeneratedException.class);

        verifyNoInteractions(aiOperationCoordinator);
    }

    @Test
    void shouldTreatLegacyAssessmentWithNullCurrentRevisionAsNotYetGenerated() {
        // Assessment.restore's legacy 4-arg shape defaults currentRevisionId to null — this must
        // NOT be mistaken for ALREADY_GENERATED; initial generation must proceed normally.
        Assessment legacyAssessment = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT, Instant.now());
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(legacyAssessment));
        when(idempotencyGuard.check(any(), eq("CREATE_INITIAL_REVISION"), eq("key-1"), any())).thenReturn(Optional.empty());
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(brief));
        AssessmentRevision revision = revision(assessmentId, "Title", 1);
        when(aiOperationCoordinator.createInitialRevision(eq(legacyAssessment), any(), eq("uid-1"), eq("key-1")))
                .thenReturn(revision);

        GenerateAssessmentDraftOutcome outcome = handler.execute(
                new GenerateAssessmentDraftCommand(assessmentUuid, "uid-1", "key-1"));

        var result = ((GenerateAssessmentDraftOutcome.RevisionCreated) outcome).revision();
        assertThat(result.title()).isEqualTo("Title");
        verify(aiOperationCoordinator).createInitialRevision(eq(legacyAssessment), any(), eq("uid-1"), eq("key-1"));
    }

    @Test
    void shouldRejectWhenAuthenticatedTeacherDoesNotOwnAssessment() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        doThrow(new ResourceNotFoundException(assessmentUuid.toString()))
                .when(ownershipVerifier).verify("uid-1", "uid-other", assessmentUuid.toString());

        assertThatThrownBy(() -> handler.execute(new GenerateAssessmentDraftCommand(assessmentUuid, "uid-other", "key-1")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(idempotencyGuard, aiOperationCoordinator, assessmentBriefRepository);
    }

    @Test
    void shouldThrowNotFoundWhenAssessmentDoesNotExist() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(new GenerateAssessmentDraftCommand(assessmentUuid, "uid-1", "key-1")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(ownershipVerifier, idempotencyGuard, aiOperationCoordinator);
    }

    @Test
    void shouldThrowNotFoundWhenBriefDoesNotExist() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(idempotencyGuard.check(any(), eq("CREATE_INITIAL_REVISION"), eq("key-1"), any())).thenReturn(Optional.empty());
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(new GenerateAssessmentDraftCommand(assessmentUuid, "uid-1", "key-1")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(aiOperationCoordinator);
    }
}
