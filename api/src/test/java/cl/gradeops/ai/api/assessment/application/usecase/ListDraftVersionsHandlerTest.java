package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.assessment.application.command.ListDraftVersionsCommand;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRevisionRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentStatus;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListDraftVersionsHandlerTest {

    @Mock AssessmentRepositoryPort assessmentRepository;
    @Mock AssessmentRevisionRepositoryPort assessmentRevisionRepository;
    @Mock OwnershipVerifier ownershipVerifier;

    ListDraftVersionsHandler handler;

    final UUID assessmentUuid = UUID.randomUUID();
    final AssessmentId assessmentId = new AssessmentId(assessmentUuid);
    final Assessment assessment = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT, Instant.now());

    @BeforeEach
    void setUp() {
        handler = new ListDraftVersionsHandler(assessmentRepository, assessmentRevisionRepository, ownershipVerifier);
    }

    @Test
    void shouldReturnAllVersionsNewestFirstAfterVerifyingOwnership() {
        AssessmentRevision v1 = AssessmentRevision.generateFromAi(assessmentId, "T1", "C1", "I1",
                List.of(), List.of(), List.of(), "uid-1", UUID.randomUUID());
        AssessmentRevision v2 = AssessmentRevision.regenerateFromAi(v1, "T2", "C2", "I2",
                List.of(), List.of(), List.of(), "uid-1", "adjust", UUID.randomUUID());
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentRevisionRepository.findAllByAssessmentId(assessmentId)).thenReturn(List.of(v2, v1));

        List<GenerateAssessmentDraftResult> result = handler.execute(new ListDraftVersionsCommand(assessmentUuid, "uid-1"));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).versionNumber()).isEqualTo(2);
        assertThat(result.get(0).title()).isEqualTo("T2");
        assertThat(result.get(1).versionNumber()).isEqualTo(1);
        assertThat(result.get(1).title()).isEqualTo("T1");
        verify(ownershipVerifier).verify("uid-1", "uid-1", assessmentUuid.toString());
    }

    @Test
    void shouldReturnEmptyListWhenNoDraftsExistYet() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentRevisionRepository.findAllByAssessmentId(assessmentId)).thenReturn(List.of());

        List<GenerateAssessmentDraftResult> result = handler.execute(new ListDraftVersionsCommand(assessmentUuid, "uid-1"));

        assertThat(result).isEmpty();
    }

    @Test
    void shouldThrowNotFoundWhenAssessmentDoesNotExist() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(new ListDraftVersionsCommand(assessmentUuid, "uid-1")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(ownershipVerifier, assessmentRevisionRepository);
    }

    @Test
    void shouldRejectWhenAuthenticatedTeacherDoesNotOwnAssessment() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        doThrow(new ResourceNotFoundException(assessmentUuid.toString()))
                .when(ownershipVerifier).verify("uid-1", "uid-other", assessmentUuid.toString());

        assertThatThrownBy(() -> handler.execute(new ListDraftVersionsCommand(assessmentUuid, "uid-other")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(assessmentRevisionRepository);
    }
}
