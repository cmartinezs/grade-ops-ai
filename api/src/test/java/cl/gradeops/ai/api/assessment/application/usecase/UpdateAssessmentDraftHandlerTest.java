package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.assessment.application.command.UpdateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.exception.NoPriorDraftException;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentDraftRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentDraft;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentStatus;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateAssessmentDraftHandlerTest {

    @Mock AssessmentRepositoryPort assessmentRepository;
    @Mock AssessmentDraftRepositoryPort assessmentDraftRepository;
    @Mock OwnershipVerifier ownershipVerifier;

    UpdateAssessmentDraftHandler handler;

    final UUID assessmentUuid = UUID.randomUUID();
    final AssessmentId assessmentId = new AssessmentId(assessmentUuid);
    final Assessment assessment = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT, Instant.now());
    final AssessmentDraft currentDraft = AssessmentDraft.generate(assessmentId, "Title v1", "Context v1",
            "Instructions v1", List.of("obj1"), List.of("del1"), List.of("con1"), UUID.randomUUID());

    @BeforeEach
    void setUp() {
        handler = new UpdateAssessmentDraftHandler(assessmentRepository, assessmentDraftRepository, ownershipVerifier);
    }

    @Test
    void shouldUpdateOnlyProvidedFieldsAndSaveWithSameIdAndVersion() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentDraftRepository.findCurrentByAssessmentId(assessmentId)).thenReturn(Optional.of(currentDraft));

        GenerateAssessmentDraftResult result = handler.execute(new UpdateAssessmentDraftCommand(
                assessmentUuid, "uid-1", "New title", null, null, null, null, null));

        assertThat(result.title()).isEqualTo("New title");
        assertThat(result.context()).isEqualTo("Context v1");
        assertThat(result.versionNumber()).isEqualTo(1);
        assertThat(result.draftId()).isEqualTo(currentDraft.getId());

        verify(ownershipVerifier).verify("uid-1", "uid-1", assessmentUuid.toString());

        ArgumentCaptor<AssessmentDraft> captor = ArgumentCaptor.forClass(AssessmentDraft.class);
        verify(assessmentDraftRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(currentDraft.getId());
        assertThat(captor.getValue().getVersionNumber()).isEqualTo(currentDraft.getVersionNumber());
        assertThat(captor.getValue().getTitle()).isEqualTo("New title");
    }

    @Test
    void shouldThrowNoPriorDraftExceptionWhenNoDraftExistsYet() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentDraftRepository.findCurrentByAssessmentId(assessmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(new UpdateAssessmentDraftCommand(
                assessmentUuid, "uid-1", "New title", null, null, null, null, null)))
                .isInstanceOf(NoPriorDraftException.class);

        verify(assessmentDraftRepository, never()).save(any());
    }

    @Test
    void shouldRejectWhenAuthenticatedTeacherDoesNotOwnAssessment() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        doThrow(new ResourceNotFoundException(assessmentUuid.toString()))
                .when(ownershipVerifier).verify("uid-1", "uid-other", assessmentUuid.toString());

        assertThatThrownBy(() -> handler.execute(new UpdateAssessmentDraftCommand(
                assessmentUuid, "uid-other", "New title", null, null, null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(assessmentDraftRepository);
    }

    @Test
    void shouldThrowNotFoundWhenAssessmentDoesNotExist() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(new UpdateAssessmentDraftCommand(
                assessmentUuid, "uid-1", "New title", null, null, null, null, null)))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(ownershipVerifier, assessmentDraftRepository);
    }
}
