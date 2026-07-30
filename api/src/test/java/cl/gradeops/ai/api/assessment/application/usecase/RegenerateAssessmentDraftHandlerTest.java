package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AssessmentCommand;
import cl.gradeops.ai.api.assessment.application.command.RegenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.exception.StaleRevisionException;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentBriefRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRevisionRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
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
 * Covers {@link RegenerateAssessmentDraftHandler}'s own responsibilities — ownership, idempotency,
 * the pre-dispatch {@code expectedRevisionId} staleness check, and delegation to {@link
 * AiOperationCoordinator} — with the coordinator mocked. The coordinator's own three-phase
 * behavior (including regenerate's CAS-at-persist-time backstop) is covered by {@link
 * AiOperationCoordinatorTest} instead; real cross-transaction concurrency is covered by {@link
 * RegenerateAssessmentDraftHandlerIntegrationTest}.
 */
@ExtendWith(MockitoExtension.class)
class RegenerateAssessmentDraftHandlerTest {

    @Mock AssessmentRepositoryPort assessmentRepository;
    @Mock AssessmentBriefRepositoryPort assessmentBriefRepository;
    @Mock AssessmentRevisionRepositoryPort assessmentRevisionRepository;
    @Mock OwnershipVerifier ownershipVerifier;
    @Mock IdempotencyGuard idempotencyGuard;
    @Mock AiOperationCoordinator aiOperationCoordinator;

    RegenerateAssessmentDraftHandler handler;

    final UUID assessmentUuid = UUID.randomUUID();
    final AssessmentId assessmentId = new AssessmentId(assessmentUuid);
    final AssessmentBrief brief = AssessmentBrief.create(assessmentId, "goal", "topic", "basic", "90min", "Java");
    final AssessmentRevision currentRevision = AssessmentRevision.generateFromAi(assessmentId, "Title v1",
            "Context v1", "Instructions v1", List.of("obj1"), List.of("del1"), List.of("con1"),
            "uid-1", UUID.randomUUID());
    final UUID currentRevisionId = currentRevision.getId();
    final Assessment assessment = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT,
            Instant.now(), currentRevisionId, 0);

    @BeforeEach
    void setUp() {
        handler = new RegenerateAssessmentDraftHandler(assessmentRepository, assessmentBriefRepository,
                assessmentRevisionRepository, ownershipVerifier, idempotencyGuard, aiOperationCoordinator);
    }

    private RegenerateAssessmentDraftCommand command(UUID expectedRevisionId) {
        return new RegenerateAssessmentDraftCommand(assessmentUuid, "uid-1", "make it harder",
                expectedRevisionId, "key-1");
    }

    @Test
    void shouldCheckIdempotencyThenStalenessThenDelegateToCoordinatorThenRecordOnSuccess() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(idempotencyGuard.check(any(), eq("REGENERATE_REVISION"), eq("key-1"), any())).thenReturn(Optional.empty());
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(brief));
        when(assessmentRevisionRepository.findById(currentRevisionId)).thenReturn(Optional.of(currentRevision));
        AssessmentRevision v2 = AssessmentRevision.regenerateFromAi(currentRevision, "Title v2", "Context v2",
                "Instructions v2", List.of("obj2"), List.of("del2"), List.of("con2"),
                "uid-1", "make it harder", UUID.randomUUID());
        when(aiOperationCoordinator.regenerateRevision(eq(assessment), eq(currentRevision), any(), eq("uid-1"),
                eq("make it harder"), eq("key-1"))).thenReturn(v2);

        GenerateAssessmentDraftResult result = handler.execute(command(currentRevisionId));

        assertThat(result.draftId()).isEqualTo(v2.getId());
        assertThat(result.title()).isEqualTo("Title v2");
        assertThat(result.versionNumber()).isEqualTo(2);
        assertThat(result.previousRevisionId()).isEqualTo(currentRevisionId);

        verify(ownershipVerifier).verify("uid-1", "uid-1", assessmentUuid.toString());

        ArgumentCaptor<IdempotencyScope> scopeCaptor = ArgumentCaptor.forClass(IdempotencyScope.class);
        verify(idempotencyGuard).check(scopeCaptor.capture(), eq("REGENERATE_REVISION"), eq("key-1"), any());
        assertThat(scopeCaptor.getValue()).isEqualTo(IdempotencyScope.assessment(assessmentUuid));

        ArgumentCaptor<AssessmentCommand> agentCommandCaptor = ArgumentCaptor.forClass(AssessmentCommand.class);
        verify(aiOperationCoordinator).regenerateRevision(eq(assessment), eq(currentRevision),
                agentCommandCaptor.capture(), eq("uid-1"), eq("make it harder"), eq("key-1"));
        assertThat(agentCommandCaptor.getValue().adjustmentNotes()).isEqualTo("make it harder");
        assertThat(agentCommandCaptor.getValue().previousDraftId()).isEqualTo(currentRevisionId.toString());

        verify(idempotencyGuard).record(eq(scopeCaptor.getValue()), eq("REGENERATE_REVISION"), eq("key-1"),
                any(), eq(v2.getId().toString()), eq(201));
    }

    @Test
    void shouldRejectWithStaleRevisionBeforeCallingCoordinatorWhenExpectedRevisionIdDoesNotMatchCurrent() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(idempotencyGuard.check(any(), eq("REGENERATE_REVISION"), eq("key-1"), any())).thenReturn(Optional.empty());
        UUID staleExpected = UUID.randomUUID();

        assertThatThrownBy(() -> handler.execute(command(staleExpected)))
                .isInstanceOf(StaleRevisionException.class);

        verifyNoInteractions(aiOperationCoordinator);
        verifyNoInteractions(assessmentBriefRepository);
    }

    @Test
    void shouldRejectWithStaleRevisionBeforeCallingCoordinatorWhenNoRevisionHasEverBeenGenerated() {
        Assessment neverGenerated = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT, Instant.now());
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(neverGenerated));
        when(idempotencyGuard.check(any(), eq("REGENERATE_REVISION"), eq("key-1"), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(command(UUID.randomUUID())))
                .isInstanceOf(StaleRevisionException.class);

        verifyNoInteractions(aiOperationCoordinator);
    }

    @Test
    void shouldPersistAdjustmentNotesAsReasonOnTheCoordinatorCall() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(idempotencyGuard.check(any(), eq("REGENERATE_REVISION"), eq("key-1"), any())).thenReturn(Optional.empty());
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(brief));
        when(assessmentRevisionRepository.findById(currentRevisionId)).thenReturn(Optional.of(currentRevision));
        AssessmentRevision v2 = AssessmentRevision.regenerateFromAi(currentRevision, "T2", "C2", "I2",
                List.of(), List.of(), List.of(), "uid-1", "make it harder", UUID.randomUUID());
        when(aiOperationCoordinator.regenerateRevision(any(), any(), any(), any(), any(), any())).thenReturn(v2);

        handler.execute(command(currentRevisionId));

        verify(aiOperationCoordinator).regenerateRevision(any(), any(), any(), any(), eq("make it harder"), any());
    }

    @Test
    void shouldReplayPriorRecordWithoutTouchingBriefOrCoordinatorWhenIdempotencyKeyAlreadyUsed() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        AssessmentRevision priorRevision = AssessmentRevision.regenerateFromAi(currentRevision, "Prior Title", "C",
                "I", List.of(), List.of(), List.of(), "uid-1", "make it harder", UUID.randomUUID());
        IdempotencyRecord priorRecord = IdempotencyRecord.create(IdempotencyScope.assessment(assessmentUuid),
                "REGENERATE_REVISION", "key-1", "hash", priorRevision.getId().toString(), 201);
        when(idempotencyGuard.check(any(), eq("REGENERATE_REVISION"), eq("key-1"), any()))
                .thenReturn(Optional.of(priorRecord));
        when(assessmentRevisionRepository.findById(priorRevision.getId())).thenReturn(Optional.of(priorRevision));

        GenerateAssessmentDraftResult result = handler.execute(command(currentRevisionId));

        assertThat(result.title()).isEqualTo("Prior Title");
        verifyNoInteractions(assessmentBriefRepository, aiOperationCoordinator);
    }

    @Test
    void shouldRejectWhenAuthenticatedTeacherDoesNotOwnAssessment() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        doThrow(new ResourceNotFoundException(assessmentUuid.toString()))
                .when(ownershipVerifier).verify("uid-1", "uid-other", assessmentUuid.toString());

        assertThatThrownBy(() -> handler.execute(new RegenerateAssessmentDraftCommand(
                assessmentUuid, "uid-other", "make it harder", currentRevisionId, "key-1")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(idempotencyGuard, aiOperationCoordinator, assessmentBriefRepository);
    }

    @Test
    void shouldThrowNotFoundWhenAssessmentDoesNotExist() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(command(currentRevisionId)))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(ownershipVerifier, idempotencyGuard, aiOperationCoordinator);
    }

    @Test
    void shouldThrowNotFoundWhenBriefDoesNotExist() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(idempotencyGuard.check(any(), eq("REGENERATE_REVISION"), eq("key-1"), any())).thenReturn(Optional.empty());
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(command(currentRevisionId)))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(aiOperationCoordinator);
    }
}
