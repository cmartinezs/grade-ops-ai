package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AssessmentAgentResponse;
import cl.gradeops.ai.api.agentclient.AssessmentCommand;
import cl.gradeops.ai.api.assessment.application.command.GenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentBriefRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
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
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Covers only this handler's own responsibilities — loading the assessment/brief, ownership
 * enforcement, and building the {@link AssessmentCommand} — with {@link DraftGenerationCoordinator}
 * mocked. The shared "call agent, persist log + draft" logic itself is covered by
 * {@link DraftGenerationCoordinatorTest} instead (task-08 extracted it out from here).
 */
@ExtendWith(MockitoExtension.class)
class GenerateAssessmentDraftHandlerTest {

    @Mock AssessmentRepositoryPort assessmentRepository;
    @Mock AssessmentBriefRepositoryPort assessmentBriefRepository;
    @Mock OwnershipVerifier ownershipVerifier;
    @Mock DraftGenerationCoordinator draftGenerationCoordinator;

    GenerateAssessmentDraftHandler handler;

    final UUID assessmentUuid = UUID.randomUUID();
    final AssessmentId assessmentId = new AssessmentId(assessmentUuid);
    final Assessment assessment = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT, Instant.now());
    final AssessmentBrief brief = AssessmentBrief.create(assessmentId, "goal", "topic", "basic", "90min", "Java");

    @BeforeEach
    void setUp() {
        handler = new GenerateAssessmentDraftHandler(
                assessmentRepository, assessmentBriefRepository, ownershipVerifier, draftGenerationCoordinator);
    }

    @Test
    void shouldVerifyOwnershipAndDelegateToCoordinatorWithCommandBuiltFromBrief() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(brief));
        GenerateAssessmentDraftResult expected = new GenerateAssessmentDraftResult(
                UUID.randomUUID(), "Title", "Context", "Instructions", List.of(), List.of(), List.of(), 1);
        when(draftGenerationCoordinator.callAgentAndPersist(eq(assessmentId), any(), any())).thenReturn(expected);

        GenerateAssessmentDraftResult result = handler.execute(new GenerateAssessmentDraftCommand(assessmentUuid, "uid-1"));

        assertThat(result).isEqualTo(expected);
        verify(ownershipVerifier).verify("uid-1", "uid-1", assessmentUuid.toString());

        ArgumentCaptor<AssessmentCommand> commandCaptor = ArgumentCaptor.forClass(AssessmentCommand.class);
        verify(draftGenerationCoordinator).callAgentAndPersist(eq(assessmentId), commandCaptor.capture(), any());
        AssessmentCommand agentCommand = commandCaptor.getValue();
        assertThat(agentCommand.learningGoal()).isEqualTo("goal");
        assertThat(agentCommand.topic()).isEqualTo("topic");
        assertThat(agentCommand.level()).isEqualTo("basic");
        assertThat(agentCommand.duration()).isEqualTo("90min");
        assertThat(agentCommand.language()).isEqualTo("Java");
        assertThat(agentCommand.adjustmentNotes()).isNull();
        assertThat(agentCommand.previousDraftId()).isNull();
        assertThat(agentCommand.previousDraft()).isNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldPassADraftFactoryThatGeneratesVersionOne() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(brief));

        handler.execute(new GenerateAssessmentDraftCommand(assessmentUuid, "uid-1"));

        @SuppressWarnings("rawtypes")
        ArgumentCaptor<BiFunction> factoryCaptor = ArgumentCaptor.forClass(BiFunction.class);
        verify(draftGenerationCoordinator).callAgentAndPersist(eq(assessmentId), any(), factoryCaptor.capture());

        UUID logId = UUID.randomUUID();
        @SuppressWarnings("unchecked")
        BiFunction<AssessmentAgentResponse.Result, UUID, cl.gradeops.ai.api.assessment.domain.model.AssessmentDraft> draftFactory =
                factoryCaptor.getValue();
        cl.gradeops.ai.api.assessment.domain.model.AssessmentDraft draft = draftFactory.apply(
                new AssessmentAgentResponse.Result("Title", "Context", "Instructions",
                        List.of("obj"), List.of("del"), List.of("con")),
                logId);

        assertThat(draft.getVersionNumber()).isEqualTo(1);
        assertThat(draft.getPreviousVersionId()).isNull();
        assertThat(draft.getAgentExecutionLogId()).isEqualTo(logId);
        assertThat(draft.getTitle()).isEqualTo("Title");
    }

    @Test
    void shouldRejectWhenAuthenticatedTeacherDoesNotOwnAssessment() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        doThrow(new ResourceNotFoundException(assessmentUuid.toString()))
                .when(ownershipVerifier).verify("uid-1", "uid-other", assessmentUuid.toString());

        assertThatThrownBy(() -> handler.execute(new GenerateAssessmentDraftCommand(assessmentUuid, "uid-other")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(draftGenerationCoordinator, assessmentBriefRepository);
    }

    @Test
    void shouldThrowNotFoundWhenAssessmentDoesNotExist() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(new GenerateAssessmentDraftCommand(assessmentUuid, "uid-1")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(ownershipVerifier, draftGenerationCoordinator);
    }

    @Test
    void shouldThrowNotFoundWhenBriefDoesNotExist() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(new GenerateAssessmentDraftCommand(assessmentUuid, "uid-1")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(draftGenerationCoordinator);
    }
}
