package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AgentClientException;
import cl.gradeops.ai.api.agentclient.AssessmentAgentClient;
import cl.gradeops.ai.api.agentclient.AssessmentAgentResponse;
import cl.gradeops.ai.api.agentclient.AssessmentCommand;
import cl.gradeops.ai.api.assessment.application.command.RegenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.exception.NoPriorDraftException;
import cl.gradeops.ai.api.assessment.application.port.out.AgentExecutionLogRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentBriefRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentDraftRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentDraft;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentStatus;
import cl.gradeops.ai.api.assessment.domain.model.AgentExecutionLog;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Still exercises the legacy {@code AssessmentDraft}/{@code AgentExecutionLog}-based
 * regeneration path — unchanged behavior from the previous shared agent-dispatch coordinator,
 * just inlined into this handler (its only remaining caller this session). See {@link
 * RegenerateAssessmentDraftHandler}'s javadoc for why Task 07B does not migrate this handler.
 */
@ExtendWith(MockitoExtension.class)
class RegenerateAssessmentDraftHandlerTest {

    @Mock AssessmentRepositoryPort assessmentRepository;
    @Mock AssessmentBriefRepositoryPort assessmentBriefRepository;
    @Mock AssessmentDraftRepositoryPort assessmentDraftRepository;
    @Mock AgentExecutionLogRepositoryPort agentExecutionLogRepository;
    @Mock OwnershipVerifier ownershipVerifier;
    @Mock AssessmentAgentClient assessmentAgentClient;
    @Mock PlatformTransactionManager transactionManager;
    @Mock TransactionStatus transactionStatus;

    RegenerateAssessmentDraftHandler handler;

    final UUID assessmentUuid = UUID.randomUUID();
    final AssessmentId assessmentId = new AssessmentId(assessmentUuid);
    final Assessment assessment = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT, Instant.now());
    final AssessmentBrief brief = AssessmentBrief.create(assessmentId, "goal", "topic", "basic", "90min", "Java");
    final AssessmentDraft currentDraft = AssessmentDraft.generate(assessmentId, "Title v1", "Context v1",
            "Instructions v1", List.of("obj1"), List.of("del1"), List.of("con1"), UUID.randomUUID());

    @BeforeEach
    void setUp() {
        lenient().when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        handler = new RegenerateAssessmentDraftHandler(assessmentRepository, assessmentBriefRepository,
                assessmentDraftRepository, agentExecutionLogRepository, ownershipVerifier,
                assessmentAgentClient, transactionManager);
    }

    private static AssessmentAgentResponse successResponse(String title) {
        Instant startedAt = Instant.now().minusSeconds(2);
        Instant finishedAt = Instant.now();
        return new AssessmentAgentResponse(
                new AssessmentAgentResponse.Result(title, "Context", "Instructions",
                        List.of("obj"), List.of("del"), List.of("con")),
                new AssessmentAgentResponse.Log(UUID.randomUUID(), "assessment", "gemini", "gemini-2.0-flash", "v1",
                        "in-hash", "out-hash", 100, 200, 0.01, "COMPLETED", null, startedAt, finishedAt));
    }

    @Test
    void shouldVerifyOwnershipAndCallAgentWithAdjustmentNotesAndPreviousDraftContent() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(brief));
        when(assessmentDraftRepository.findCurrentByAssessmentId(assessmentId)).thenReturn(Optional.of(currentDraft));
        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(successResponse("Title v2"));

        GenerateAssessmentDraftResult result = handler.execute(
                new RegenerateAssessmentDraftCommand(assessmentUuid, "uid-1", "make it harder"));

        assertThat(result.title()).isEqualTo("Title v2");
        assertThat(result.versionNumber()).isEqualTo(2);
        verify(ownershipVerifier).verify("uid-1", "uid-1", assessmentUuid.toString());

        ArgumentCaptor<AssessmentCommand> commandCaptor = ArgumentCaptor.forClass(AssessmentCommand.class);
        verify(assessmentAgentClient).generate(commandCaptor.capture(), anyString());
        AssessmentCommand agentCommand = commandCaptor.getValue();
        assertThat(agentCommand.adjustmentNotes()).isEqualTo("make it harder");
        assertThat(agentCommand.previousDraftId()).isEqualTo(currentDraft.getId().toString());
        assertThat(agentCommand.previousDraft()).contains("Title v1", "Context v1", "Instructions v1", "obj1", "del1", "con1");
    }

    @Test
    void shouldPersistNewDraftVersionChainedToThePreviousOne() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(brief));
        when(assessmentDraftRepository.findCurrentByAssessmentId(assessmentId)).thenReturn(Optional.of(currentDraft));
        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(successResponse("Title v2"));

        handler.execute(new RegenerateAssessmentDraftCommand(assessmentUuid, "uid-1", "make it harder"));

        ArgumentCaptor<AssessmentDraft> draftCaptor = ArgumentCaptor.forClass(AssessmentDraft.class);
        verify(assessmentDraftRepository).save(draftCaptor.capture());
        assertThat(draftCaptor.getValue().getVersionNumber()).isEqualTo(2);
        assertThat(draftCaptor.getValue().getPreviousVersionId()).isEqualTo(currentDraft.getId());
        assertThat(draftCaptor.getValue().getTitle()).isEqualTo("Title v2");
    }

    @Test
    void shouldPersistFailureLogWithoutDraftAndRethrowWhenAgentCallFails() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(brief));
        when(assessmentDraftRepository.findCurrentByAssessmentId(assessmentId)).thenReturn(Optional.of(currentDraft));
        AgentClientException agentEx = new AgentClientException(
                AgentClientException.Reason.AGENT_REJECTED, "rejected", new RuntimeException());
        when(assessmentAgentClient.generate(any(), anyString())).thenThrow(agentEx);

        assertThatThrownBy(() -> handler.execute(
                new RegenerateAssessmentDraftCommand(assessmentUuid, "uid-1", "make it harder")))
                .isSameAs(agentEx);

        ArgumentCaptor<AgentExecutionLog> logCaptor = ArgumentCaptor.forClass(AgentExecutionLog.class);
        verify(agentExecutionLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getStatus()).isEqualTo("FAILED");
        assertThat(logCaptor.getValue().getErrorCode()).isEqualTo("AGENT_REJECTED");

        // assessmentDraftRepository.findCurrentByAssessmentId was legitimately called earlier
        // (to load currentDraft) — what must NOT happen on failure is persisting a new draft row.
        verify(assessmentDraftRepository, never()).save(any());
    }

    @Test
    void shouldThrowNoPriorDraftExceptionWhenNoDraftExistsYet() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(brief));
        when(assessmentDraftRepository.findCurrentByAssessmentId(assessmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(
                new RegenerateAssessmentDraftCommand(assessmentUuid, "uid-1", "make it harder")))
                .isInstanceOf(NoPriorDraftException.class);

        verifyNoInteractions(assessmentAgentClient);
    }

    @Test
    void shouldRejectWhenAuthenticatedTeacherDoesNotOwnAssessment() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        doThrow(new ResourceNotFoundException(assessmentUuid.toString()))
                .when(ownershipVerifier).verify("uid-1", "uid-other", assessmentUuid.toString());

        assertThatThrownBy(() -> handler.execute(
                new RegenerateAssessmentDraftCommand(assessmentUuid, "uid-other", "make it harder")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(assessmentAgentClient, assessmentBriefRepository, assessmentDraftRepository);
    }

    @Test
    void shouldThrowNotFoundWhenAssessmentDoesNotExist() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(
                new RegenerateAssessmentDraftCommand(assessmentUuid, "uid-1", "make it harder")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(ownershipVerifier, assessmentAgentClient);
    }
}
