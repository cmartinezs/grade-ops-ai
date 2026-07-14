package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AgentClientException;
import cl.gradeops.ai.api.agentclient.AssessmentAgentClient;
import cl.gradeops.ai.api.agentclient.AssessmentAgentResponse;
import cl.gradeops.ai.api.assessment.application.command.GenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.port.out.AgentExecutionLogRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentBriefRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentDraftRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.AgentExecutionLog;
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
import org.mockito.InOrder;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateAssessmentDraftHandlerTest {

    @Mock AssessmentRepositoryPort assessmentRepository;
    @Mock AssessmentBriefRepositoryPort assessmentBriefRepository;
    @Mock AssessmentDraftRepositoryPort assessmentDraftRepository;
    @Mock AgentExecutionLogRepositoryPort agentExecutionLogRepository;
    @Mock AssessmentAgentClient assessmentAgentClient;
    @Mock OwnershipVerifier ownershipVerifier;
    @Mock PlatformTransactionManager transactionManager;
    @Mock TransactionStatus transactionStatus;

    GenerateAssessmentDraftHandler handler;

    final UUID assessmentUuid = UUID.randomUUID();
    final AssessmentId assessmentId = new AssessmentId(assessmentUuid);
    final Assessment assessment = Assessment.restore(assessmentId, "uid-1", AssessmentStatus.DRAFT, Instant.now());
    final AssessmentBrief brief = AssessmentBrief.create(assessmentId, "goal", "topic", "basic", "90min", "Java");

    @BeforeEach
    void setUp() {
        lenient().when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        handler = new GenerateAssessmentDraftHandler(assessmentRepository, assessmentBriefRepository,
                assessmentDraftRepository, agentExecutionLogRepository, assessmentAgentClient,
                ownershipVerifier, transactionManager);
    }

    private static AssessmentAgentResponse successResponse() {
        Instant startedAt = Instant.now().minusSeconds(2);
        Instant finishedAt = Instant.now();
        return new AssessmentAgentResponse(
                new AssessmentAgentResponse.Result("Title", "Context", "Instructions",
                        List.of("obj"), List.of("del"), List.of("con")),
                new AssessmentAgentResponse.Log(UUID.randomUUID(), "assessment", "gemini-2.0-flash", "v1",
                        "in-hash", "out-hash", 100, 200, 0.01, "COMPLETED", null, startedAt, finishedAt));
    }

    @Test
    void shouldPersistDraftAndSuccessLogWhenAgentCallSucceeds() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(brief));
        when(assessmentAgentClient.generate(any())).thenReturn(successResponse());

        GenerateAssessmentDraftResult result = handler.execute(new GenerateAssessmentDraftCommand(assessmentUuid, "uid-1"));

        assertThat(result.title()).isEqualTo("Title");
        assertThat(result.versionNumber()).isEqualTo(1);

        verify(ownershipVerifier).verify("uid-1", "uid-1", assessmentUuid.toString());

        ArgumentCaptor<AgentExecutionLog> logCaptor = ArgumentCaptor.forClass(AgentExecutionLog.class);
        verify(agentExecutionLogRepository, times(2)).save(logCaptor.capture());
        assertThat(logCaptor.getAllValues().get(0).getDraftId()).isNull();
        assertThat(logCaptor.getAllValues().get(0).getStatus()).isEqualTo("COMPLETED");
        assertThat(logCaptor.getAllValues().get(1).getDraftId()).isNotNull();

        ArgumentCaptor<AssessmentDraft> draftCaptor = ArgumentCaptor.forClass(AssessmentDraft.class);
        verify(assessmentDraftRepository).save(draftCaptor.capture());
        assertThat(draftCaptor.getValue().getAgentExecutionLogId()).isEqualTo(logCaptor.getAllValues().get(0).getId());
        assertThat(draftCaptor.getValue().getVersionNumber()).isEqualTo(1);
    }

    @Test
    void shouldPersistFailureLogWithoutDraftAndRethrowWhenAgentCallFails() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(brief));

        AgentClientException agentEx = new AgentClientException(
                AgentClientException.Reason.AGENT_REJECTED, "rejected", new RuntimeException());
        when(assessmentAgentClient.generate(any())).thenThrow(agentEx);

        assertThatThrownBy(() -> handler.execute(new GenerateAssessmentDraftCommand(assessmentUuid, "uid-1")))
                .isSameAs(agentEx);

        ArgumentCaptor<AgentExecutionLog> logCaptor = ArgumentCaptor.forClass(AgentExecutionLog.class);
        verify(agentExecutionLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getStatus()).isEqualTo("FAILED");
        assertThat(logCaptor.getValue().getErrorCode()).isEqualTo("AGENT_REJECTED");
        assertThat(logCaptor.getValue().getDraftId()).isNull();

        verifyNoInteractions(assessmentDraftRepository);
    }

    @Test
    void shouldRejectWhenAuthenticatedTeacherDoesNotOwnAssessment() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        doThrow(new ResourceNotFoundException(assessmentUuid.toString()))
                .when(ownershipVerifier).verify("uid-1", "uid-other", assessmentUuid.toString());

        assertThatThrownBy(() -> handler.execute(new GenerateAssessmentDraftCommand(assessmentUuid, "uid-other")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(assessmentAgentClient, assessmentBriefRepository);
    }

    @Test
    void shouldThrowNotFoundWhenAssessmentDoesNotExist() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(new GenerateAssessmentDraftCommand(assessmentUuid, "uid-1")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(ownershipVerifier, assessmentAgentClient);
    }

    @Test
    void shouldThrowNotFoundWhenBriefDoesNotExist() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.execute(new GenerateAssessmentDraftCommand(assessmentUuid, "uid-1")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(assessmentAgentClient);
    }

    @Test
    void shouldCallAgentClientBeforeOpeningAnyDatabaseTransaction() {
        when(assessmentRepository.findById(assessmentId)).thenReturn(Optional.of(assessment));
        when(assessmentBriefRepository.findByAssessmentId(assessmentId)).thenReturn(Optional.of(brief));
        when(assessmentAgentClient.generate(any())).thenReturn(successResponse());

        handler.execute(new GenerateAssessmentDraftCommand(assessmentUuid, "uid-1"));

        InOrder inOrder = inOrder(assessmentAgentClient, transactionManager);
        inOrder.verify(assessmentAgentClient).generate(any());
        inOrder.verify(transactionManager).getTransaction(any());
    }
}
