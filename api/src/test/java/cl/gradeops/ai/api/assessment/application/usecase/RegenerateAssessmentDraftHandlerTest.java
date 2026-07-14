package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AssessmentAgentResponse;
import cl.gradeops.ai.api.agentclient.AssessmentCommand;
import cl.gradeops.ai.api.assessment.application.command.RegenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.exception.NoPriorDraftException;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentBriefRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentDraftRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
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
import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegenerateAssessmentDraftHandlerTest {

    @Mock AssessmentRepositoryPort assessmentRepository;
    @Mock AssessmentBriefRepositoryPort assessmentBriefRepository;
    @Mock AssessmentDraftRepositoryPort assessmentDraftRepository;
    @Mock OwnershipVerifier ownershipVerifier;
    @Mock DraftGenerationCoordinator draftGenerationCoordinator;

    RegenerateAssessmentDraftHandler handler;

    final UUID assessmentUuid = UUID.randomUUID();
    final AssessmentId assessmentId = new AssessmentId(assessmentUuid);
    final Assessment assessment = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT, Instant.now());
    final AssessmentBrief brief = AssessmentBrief.create(assessmentId, "goal", "topic", "basic", "90min", "Java");
    final AssessmentDraft currentDraft = AssessmentDraft.generate(assessmentId, "Title v1", "Context v1",
            "Instructions v1", List.of("obj1"), List.of("del1"), List.of("con1"), UUID.randomUUID());

    @BeforeEach
    void setUp() {
        handler = new RegenerateAssessmentDraftHandler(assessmentRepository, assessmentBriefRepository,
                assessmentDraftRepository, ownershipVerifier, draftGenerationCoordinator);
    }

    @Test
    void shouldVerifyOwnershipAndDelegateWithAdjustmentNotesAndPreviousDraftContent() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(brief));
        when(assessmentDraftRepository.findCurrentByAssessmentId(assessmentId)).thenReturn(Optional.of(currentDraft));
        GenerateAssessmentDraftResult expected = new GenerateAssessmentDraftResult(
                UUID.randomUUID(), "Title v2", "Context v2", "Instructions v2", List.of(), List.of(), List.of(), 2);
        when(draftGenerationCoordinator.callAgentAndPersist(eq(assessmentId), any(), any())).thenReturn(expected);

        GenerateAssessmentDraftResult result = handler.execute(
                new RegenerateAssessmentDraftCommand(assessmentUuid, "uid-1", "make it harder"));

        assertThat(result).isEqualTo(expected);
        verify(ownershipVerifier).verify("uid-1", "uid-1", assessmentUuid.toString());

        ArgumentCaptor<AssessmentCommand> commandCaptor = ArgumentCaptor.forClass(AssessmentCommand.class);
        verify(draftGenerationCoordinator).callAgentAndPersist(eq(assessmentId), commandCaptor.capture(), any());
        AssessmentCommand agentCommand = commandCaptor.getValue();
        assertThat(agentCommand.adjustmentNotes()).isEqualTo("make it harder");
        assertThat(agentCommand.previousDraftId()).isEqualTo(currentDraft.getId().toString());
        assertThat(agentCommand.previousDraft()).contains("Title v1", "Context v1", "Instructions v1", "obj1", "del1", "con1");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void shouldPassADraftFactoryThatRegeneratesFromCurrentVersion() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(brief));
        when(assessmentDraftRepository.findCurrentByAssessmentId(assessmentId)).thenReturn(Optional.of(currentDraft));

        handler.execute(new RegenerateAssessmentDraftCommand(assessmentUuid, "uid-1", "make it harder"));

        ArgumentCaptor<BiFunction> factoryCaptor = ArgumentCaptor.forClass(BiFunction.class);
        verify(draftGenerationCoordinator).callAgentAndPersist(eq(assessmentId), any(), factoryCaptor.capture());

        UUID logId = UUID.randomUUID();
        BiFunction<AssessmentAgentResponse.Result, UUID, AssessmentDraft> draftFactory = factoryCaptor.getValue();
        AssessmentDraft newDraft = draftFactory.apply(
                new AssessmentAgentResponse.Result("Title v2", "Context v2", "Instructions v2",
                        List.of("obj2"), List.of("del2"), List.of("con2")),
                logId);

        assertThat(newDraft.getVersionNumber()).isEqualTo(2);
        assertThat(newDraft.getPreviousVersionId()).isEqualTo(currentDraft.getId());
        assertThat(newDraft.getAgentExecutionLogId()).isEqualTo(logId);
        assertThat(newDraft.getTitle()).isEqualTo("Title v2");
    }

    @Test
    void shouldThrowNoPriorDraftExceptionWhenNoDraftExistsYet() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(brief));
        when(assessmentDraftRepository.findCurrentByAssessmentId(assessmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(
                new RegenerateAssessmentDraftCommand(assessmentUuid, "uid-1", "make it harder")))
                .isInstanceOf(NoPriorDraftException.class);

        verifyNoInteractions(draftGenerationCoordinator);
    }

    @Test
    void shouldRejectWhenAuthenticatedTeacherDoesNotOwnAssessment() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        doThrow(new ResourceNotFoundException(assessmentUuid.toString()))
                .when(ownershipVerifier).verify("uid-1", "uid-other", assessmentUuid.toString());

        assertThatThrownBy(() -> handler.execute(
                new RegenerateAssessmentDraftCommand(assessmentUuid, "uid-other", "make it harder")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(draftGenerationCoordinator, assessmentBriefRepository, assessmentDraftRepository);
    }

    @Test
    void shouldThrowNotFoundWhenAssessmentDoesNotExist() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(
                new RegenerateAssessmentDraftCommand(assessmentUuid, "uid-1", "make it harder")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(ownershipVerifier, draftGenerationCoordinator);
    }
}
