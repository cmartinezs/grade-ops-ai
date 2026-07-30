package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.assessment.application.command.GetCurrentDraftCommand;
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
class GetCurrentDraftHandlerTest {

    @Mock AssessmentRepositoryPort assessmentRepository;
    @Mock AssessmentRevisionRepositoryPort assessmentRevisionRepository;
    @Mock OwnershipVerifier ownershipVerifier;

    GetCurrentDraftHandler handler;

    final UUID assessmentUuid = UUID.randomUUID();
    final AssessmentId assessmentId = new AssessmentId(assessmentUuid);
    final AssessmentRevision currentRevision = AssessmentRevision.generateFromAi(assessmentId, "Title", "Context",
            "Instructions", List.of("obj"), List.of("del"), List.of("con"), "uid-1", UUID.randomUUID());
    final Assessment assessment = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT, Instant.now(),
            currentRevision.getId(), 0);
    final Assessment assessmentWithoutRevision = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT, Instant.now());

    @BeforeEach
    void setUp() {
        handler = new GetCurrentDraftHandler(assessmentRepository, assessmentRevisionRepository, ownershipVerifier);
    }

    @Test
    void shouldReturnCurrentDraftAfterVerifyingOwnership() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentRevisionRepository.findById(currentRevision.getId())).thenReturn(Optional.of(currentRevision));

        GenerateAssessmentDraftResult result = handler.execute(new GetCurrentDraftCommand(assessmentUuid, "uid-1"));

        assertThat(result.draftId()).isEqualTo(currentRevision.getId());
        assertThat(result.title()).isEqualTo("Title");
        verify(ownershipVerifier).verify("uid-1", "uid-1", assessmentUuid.toString());
    }

    @Test
    void shouldThrowNotFoundWhenNoDraftExistsYet() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessmentWithoutRevision));

        assertThatThrownBy(() -> handler.execute(new GetCurrentDraftCommand(assessmentUuid, "uid-1")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(assessmentRevisionRepository);
    }

    @Test
    void shouldThrowNotFoundWhenAssessmentDoesNotExist() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(new GetCurrentDraftCommand(assessmentUuid, "uid-1")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(ownershipVerifier, assessmentRevisionRepository);
    }

    @Test
    void shouldRejectWhenAuthenticatedTeacherDoesNotOwnAssessment() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        doThrow(new ResourceNotFoundException(assessmentUuid.toString()))
                .when(ownershipVerifier).verify("uid-1", "uid-other", assessmentUuid.toString());

        assertThatThrownBy(() -> handler.execute(new GetCurrentDraftCommand(assessmentUuid, "uid-other")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(assessmentRevisionRepository);
    }
}
