package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AgentClientException;
import cl.gradeops.ai.api.agentclient.AssessmentAgentClient;
import cl.gradeops.ai.api.agentclient.AssessmentAgentResponse;
import cl.gradeops.ai.api.agentclient.AssessmentCommand;
import cl.gradeops.ai.api.assessment.application.port.out.AgentExecutionLogRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentDraftRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.AgentExecutionLog;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentDraft;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DraftGenerationCoordinatorTest {

    @Mock AssessmentDraftRepositoryPort assessmentDraftRepository;
    @Mock AgentExecutionLogRepositoryPort agentExecutionLogRepository;
    @Mock AssessmentAgentClient assessmentAgentClient;
    @Mock PlatformTransactionManager transactionManager;
    @Mock TransactionStatus transactionStatus;

    DraftGenerationCoordinator coordinator;

    final AssessmentId assessmentId = new AssessmentId(UUID.randomUUID());
    final AssessmentCommand agentCommand = new AssessmentCommand(
            "goal", "topic", "basic", "90min", "Java", null, null, null, null, null);

    @BeforeEach
    void setUp() {
        lenient().when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        coordinator = new DraftGenerationCoordinator(
                assessmentDraftRepository, agentExecutionLogRepository, assessmentAgentClient, transactionManager);
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

    private static AssessmentDraft generateV1(AssessmentId assessmentId, AssessmentAgentResponse.Result result, UUID logId) {
        return AssessmentDraft.generate(assessmentId, result.title(), result.context(), result.instructions(),
                result.objectives(), result.deliverables(), result.constraints(), logId);
    }

    @Test
    void shouldPersistDraftAndSuccessLogWhenAgentCallSucceeds() {
        when(assessmentAgentClient.generate(any())).thenReturn(successResponse());

        GenerateAssessmentDraftResult result = coordinator.callAgentAndPersist(assessmentId, agentCommand,
                (r, logId) -> generateV1(assessmentId, r, logId));

        assertThat(result.title()).isEqualTo("Title");
        assertThat(result.versionNumber()).isEqualTo(1);

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
        AgentClientException agentEx = new AgentClientException(
                AgentClientException.Reason.AGENT_REJECTED, "rejected", new RuntimeException());
        when(assessmentAgentClient.generate(any())).thenThrow(agentEx);

        assertThatThrownBy(() -> coordinator.callAgentAndPersist(assessmentId, agentCommand,
                (r, logId) -> generateV1(assessmentId, r, logId)))
                .isSameAs(agentEx);

        ArgumentCaptor<AgentExecutionLog> logCaptor = ArgumentCaptor.forClass(AgentExecutionLog.class);
        verify(agentExecutionLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getStatus()).isEqualTo("FAILED");
        assertThat(logCaptor.getValue().getErrorCode()).isEqualTo("AGENT_REJECTED");
        assertThat(logCaptor.getValue().getDraftId()).isNull();

        verifyNoInteractions(assessmentDraftRepository);
    }

    @Test
    void shouldCallAgentClientBeforeOpeningAnyDatabaseTransaction() {
        when(assessmentAgentClient.generate(any())).thenReturn(successResponse());

        coordinator.callAgentAndPersist(assessmentId, agentCommand, (r, logId) -> generateV1(assessmentId, r, logId));

        InOrder inOrder = inOrder(assessmentAgentClient, transactionManager);
        inOrder.verify(assessmentAgentClient).generate(any());
        inOrder.verify(transactionManager).getTransaction(any());
    }

    @Test
    void shouldUseDraftFactoryToBuildTheDraftFromTheAgentResultAndPersistedLogId() {
        when(assessmentAgentClient.generate(any())).thenReturn(successResponse());

        coordinator.callAgentAndPersist(assessmentId, agentCommand, (r, logId) -> {
            assertThat(r.title()).isEqualTo("Title");
            assertThat(logId).isNotNull();
            return generateV1(assessmentId, r, logId);
        });

        verify(assessmentDraftRepository).save(any());
    }
}
