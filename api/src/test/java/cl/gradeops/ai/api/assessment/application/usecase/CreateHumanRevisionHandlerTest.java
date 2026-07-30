package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.assessment.application.command.CreateHumanRevisionCommand;
import cl.gradeops.ai.api.assessment.application.exception.StaleRevisionException;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRevisionRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentStatus;
import cl.gradeops.ai.api.assessment.domain.model.RevisionOrigin;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Covers {@link CreateHumanRevisionHandler}'s own responsibilities with mocked ports — the CAS
 * race that can only be detected by a real DB-level optimistic lock is covered by {@link
 * CreateHumanRevisionHandlerIntegrationTest} (Testcontainers, two real concurrent transactions).
 */
@ExtendWith(MockitoExtension.class)
class CreateHumanRevisionHandlerTest {

    @Mock AssessmentRepositoryPort assessmentRepository;
    @Mock AssessmentRevisionRepositoryPort assessmentRevisionRepository;
    @Mock OwnershipVerifier ownershipVerifier;
    @Mock PlatformTransactionManager transactionManager;
    @Mock TransactionStatus transactionStatus;

    CreateHumanRevisionHandler handler;

    final UUID assessmentUuid = UUID.randomUUID();
    final AssessmentId assessmentId = new AssessmentId(assessmentUuid);
    final AssessmentRevision currentRevision = AssessmentRevision.generateFromAi(assessmentId, "Title v1",
            "Context v1", "Instructions v1", List.of("obj1"), List.of("del1"), List.of("con1"),
            "uid-1", UUID.randomUUID());
    final UUID currentRevisionId = currentRevision.getId();
    final Assessment assessment = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT,
            Instant.now(), currentRevisionId, 0);

    @BeforeEach
    void setUp() {
        lenient().when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        handler = new CreateHumanRevisionHandler(assessmentRepository, assessmentRevisionRepository,
                ownershipVerifier, transactionManager);
    }

    private CreateHumanRevisionCommand editCommand() {
        return new CreateHumanRevisionCommand(assessmentUuid, "uid-1", currentRevisionId,
                "Edited title", "Edited context", "Edited instructions",
                List.of("obj2"), List.of("del2"), List.of("con2"), "fixed a typo");
    }

    @Test
    void shouldCreateNewRevisionChainedToTheExpectedOneOnSuccess() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentRevisionRepository.findById(currentRevisionId)).thenReturn(Optional.of(currentRevision));

        GenerateAssessmentDraftResult result = handler.execute(editCommand());

        assertThat(result.title()).isEqualTo("Edited title");
        assertThat(result.versionNumber()).isEqualTo(2);
        assertThat(result.previousRevisionId()).isEqualTo(currentRevisionId);

        verify(ownershipVerifier).verify("uid-1", "uid-1", assessmentUuid.toString());

        ArgumentCaptor<AssessmentRevision> revisionCaptor = ArgumentCaptor.forClass(AssessmentRevision.class);
        verify(assessmentRevisionRepository).save(revisionCaptor.capture());
        AssessmentRevision saved = revisionCaptor.getValue();
        assertThat(saved.getOrigin()).isEqualTo(RevisionOrigin.HUMAN_EDITED);
        assertThat(saved.getPreviousRevisionId()).isEqualTo(currentRevisionId);
        assertThat(saved.getVersionNumber()).isEqualTo(2);

        verify(assessmentRepository).save(argThat(a -> saved.getId().equals(a.getCurrentRevisionId())));
    }

    @Test
    void shouldSetActorIdFromAuthenticatedTeacherNotAnyClientSuppliedValue() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentRevisionRepository.findById(currentRevisionId)).thenReturn(Optional.of(currentRevision));

        GenerateAssessmentDraftResult result = handler.execute(editCommand());

        assertThat(result.actorId()).isEqualTo("uid-1");
        assertThat(result.origin()).isEqualTo("HUMAN_EDITED");
        assertThat(result.reason()).isEqualTo("fixed a typo");
    }

    @Test
    void shouldNeverMutateThePreviousRevisionsOwnContent() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentRevisionRepository.findById(currentRevisionId)).thenReturn(Optional.of(currentRevision));

        handler.execute(editCommand());

        assertThat(currentRevision.getTitle()).isEqualTo("Title v1");
        assertThat(currentRevision.getOrigin()).isEqualTo(RevisionOrigin.AI_GENERATED);
    }

    @Test
    void shouldThrowStaleRevisionWhenExpectedRevisionIdDoesNotMatchCurrent() {
        UUID staleExpected = UUID.randomUUID();
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));

        assertThatThrownBy(() -> handler.execute(new CreateHumanRevisionCommand(assessmentUuid, "uid-1",
                staleExpected, "t", "c", "i", List.of(), List.of(), List.of(), null)))
                .isInstanceOf(StaleRevisionException.class);

        verifyNoInteractions(assessmentRevisionRepository);
    }

    @Test
    void shouldThrowStaleRevisionWhenNoRevisionHasEverBeenGenerated() {
        Assessment neverGenerated = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT, Instant.now());
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(neverGenerated));

        assertThatThrownBy(() -> handler.execute(editCommand()))
                .isInstanceOf(StaleRevisionException.class);

        verifyNoInteractions(assessmentRevisionRepository);
    }

    @Test
    void shouldThrowStaleRevisionWhenConcurrentWriterWinsTheCasRace() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentRevisionRepository.findById(currentRevisionId)).thenReturn(Optional.of(currentRevision));
        doThrow(new ObjectOptimisticLockingFailureException(Assessment.class, assessmentUuid.toString()))
                .when(assessmentRepository).save(argThat(a -> a.getCurrentRevisionId() != null
                        && !a.getCurrentRevisionId().equals(currentRevisionId)));

        assertThatThrownBy(() -> handler.execute(editCommand()))
                .isInstanceOf(StaleRevisionException.class);
    }

    @Test
    void shouldRejectWhenAuthenticatedTeacherDoesNotOwnAssessmentBeforeTouchingRevisions() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        doThrow(new ResourceNotFoundException(assessmentUuid.toString()))
                .when(ownershipVerifier).verify("uid-1", "uid-other", assessmentUuid.toString());

        assertThatThrownBy(() -> handler.execute(new CreateHumanRevisionCommand(assessmentUuid, "uid-other",
                currentRevisionId, "t", "c", "i", List.of(), List.of(), List.of(), null)))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(assessmentRevisionRepository);
        verifyNoMoreInteractions(transactionManager);
    }

    @Test
    void shouldThrowNotFoundWhenAssessmentDoesNotExist() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(editCommand()))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(ownershipVerifier, assessmentRevisionRepository);
    }
}
