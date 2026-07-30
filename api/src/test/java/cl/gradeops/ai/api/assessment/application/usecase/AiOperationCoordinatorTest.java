package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AgentClientException;
import cl.gradeops.ai.api.agentclient.AssessmentAgentClient;
import cl.gradeops.ai.api.agentclient.AssessmentAgentErrorPayload;
import cl.gradeops.ai.api.agentclient.AssessmentAgentResponse;
import cl.gradeops.ai.api.agentclient.AssessmentCommand;
import cl.gradeops.ai.api.assessment.application.exception.OperationInProgressException;
import cl.gradeops.ai.api.assessment.application.exception.StaleOnCompletionException;
import cl.gradeops.ai.api.assessment.application.exception.StaleRevisionException;
import cl.gradeops.ai.api.assessment.application.port.out.AgentAttemptRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AiOperationRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRevisionRepositoryPort;
import cl.gradeops.ai.api.assessment.domain.model.AgentAttempt;
import cl.gradeops.ai.api.assessment.domain.model.AgentAttemptStatus;
import cl.gradeops.ai.api.assessment.domain.model.AiOperation;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationStatus;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiOperationCoordinatorTest {

    @Mock AssessmentRepositoryPort assessmentRepository;
    @Mock AiOperationRepositoryPort aiOperationRepository;
    @Mock AgentAttemptRepositoryPort agentAttemptRepository;
    @Mock AssessmentRevisionRepositoryPort assessmentRevisionRepository;
    @Mock AssessmentAgentClient assessmentAgentClient;
    @Mock PlatformTransactionManager transactionManager;
    @Mock TransactionStatus transactionStatus;

    final JsonMapper jsonMapper = JsonMapper.builder().build();

    AiOperationCoordinator coordinator;

    final Assessment assessment = Assessment.create("uid-1");
    final AssessmentCommand agentCommand = new AssessmentCommand(
            "goal", "topic", "basic", "90min", "Java", null, null, null, null, null);

    @BeforeEach
    void setUp() {
        lenient().when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        coordinator = new AiOperationCoordinator(assessmentRepository, aiOperationRepository, agentAttemptRepository,
                assessmentRevisionRepository, assessmentAgentClient, jsonMapper, transactionManager);
        lenient().when(assessmentRepository.findById(assessment.getId())).thenReturn(Optional.of(assessment));
    }

    private static AssessmentAgentResponse successResponse() {
        Instant startedAt = Instant.now().minusSeconds(2);
        Instant finishedAt = Instant.now();
        return new AssessmentAgentResponse(
                new AssessmentAgentResponse.Result("Title", "Context", "Instructions",
                        List.of("obj"), List.of("del"), List.of("con")),
                new AssessmentAgentResponse.Log(UUID.randomUUID(), "assessment", "gemini", "gemini-2.0-flash", "v1",
                        "in-hash", "out-hash", 100, 200, 0.01, "COMPLETED", null, startedAt, finishedAt));
    }

    @Test
    void shouldPersistDurableEvidenceBeforeCallingTheAgent() {
        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(successResponse());

        coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1");

        InOrder inOrder = inOrder(aiOperationRepository, agentAttemptRepository, assessmentAgentClient);
        inOrder.verify(aiOperationRepository).save(argThat(op -> op.getStatus() == AiOperationStatus.IN_PROGRESS));
        inOrder.verify(agentAttemptRepository).save(argThat(a -> a.getStatus() == AgentAttemptStatus.DISPATCHED));
        inOrder.verify(assessmentAgentClient).generate(any(), anyString());
    }

    @Test
    void shouldNotOpenAnyTransactionDuringTheHttpCall() {
        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(successResponse());

        coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1");

        InOrder inOrder = inOrder(transactionManager, assessmentAgentClient);
        inOrder.verify(transactionManager).getTransaction(any());
        inOrder.verify(assessmentAgentClient).generate(any(), anyString());
        inOrder.verify(transactionManager, atLeastOnce()).getTransaction(any());
    }

    @Test
    void shouldSurviveAgentCallFailureWithPhase0EvidenceAlreadyPersisted() {
        AgentClientException agentEx = new AgentClientException(
                AgentClientException.Reason.UNREACHABLE, "unreachable", new RuntimeException());
        when(assessmentAgentClient.generate(any(), anyString())).thenThrow(agentEx);

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1"))
                .isSameAs(agentEx);

        // Phase 0's AiOperation/AgentAttempt were saved BEFORE the agent call ran at all — the
        // durable-evidence-before-dispatch property the old (now-removed) coordinator lacked,
        // since it called the agent first and persisted nothing until after the response.
        verify(aiOperationRepository).save(argThat(op -> op.getStatus() == AiOperationStatus.IN_PROGRESS));
        verify(agentAttemptRepository).save(argThat(a -> a.getStatus() == AgentAttemptStatus.DISPATCHED));
    }

    @Test
    void shouldPersistCompletedAttemptSucceededOperationAndCreateRevisionOnSuccess() {
        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(successResponse());

        AssessmentRevision revision = coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1");

        assertThat(revision.getTitle()).isEqualTo("Title");
        assertThat(revision.getVersionNumber()).isEqualTo(1);
        assertThat(revision.getActorId()).isEqualTo("uid-1");

        ArgumentCaptor<AgentAttempt> attemptCaptor = ArgumentCaptor.forClass(AgentAttempt.class);
        verify(agentAttemptRepository, times(2)).save(attemptCaptor.capture());
        AgentAttempt completedAttempt = attemptCaptor.getAllValues().get(1);
        assertThat(completedAttempt.getStatus()).isEqualTo(AgentAttemptStatus.COMPLETED);
        assertThat(completedAttempt.getResolvedProvider()).isEqualTo("gemini");
        assertThat(completedAttempt.getResolvedModel()).isEqualTo("gemini-2.0-flash");
        assertThat(completedAttempt.getStructuredResult()).contains("Title");

        ArgumentCaptor<AiOperation> operationCaptor = ArgumentCaptor.forClass(AiOperation.class);
        verify(aiOperationRepository, times(2)).save(operationCaptor.capture());
        AiOperation succeededOperation = operationCaptor.getAllValues().get(1);
        assertThat(succeededOperation.getStatus()).isEqualTo(AiOperationStatus.SUCCEEDED);
        assertThat(succeededOperation.getResultRevisionId()).isEqualTo(revision.getId());

        verify(assessmentRevisionRepository).save(revision);
        verify(assessmentRepository).save(argThat(a -> revision.getId().equals(a.getCurrentRevisionId())));
    }

    @Test
    void shouldPropagateTheSameCorrelationIdFromDispatchToTheAgentCall() {
        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(successResponse());

        coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1");

        ArgumentCaptor<AgentAttempt> attemptCaptor = ArgumentCaptor.forClass(AgentAttempt.class);
        verify(agentAttemptRepository, atLeastOnce()).save(attemptCaptor.capture());
        String dispatchedCorrelationId = attemptCaptor.getAllValues().get(0).getCorrelationId();

        ArgumentCaptor<String> correlationIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(assessmentAgentClient).generate(any(), correlationIdCaptor.capture());
        assertThat(correlationIdCaptor.getValue()).isEqualTo(dispatchedCorrelationId);
    }

    @Test
    void shouldMarkFailedRetryableOnTransportUnreachable() {
        AgentClientException agentEx = new AgentClientException(
                AgentClientException.Reason.UNREACHABLE, "unreachable", new RuntimeException());
        when(assessmentAgentClient.generate(any(), anyString())).thenThrow(agentEx);

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1"))
                .isSameAs(agentEx);

        verify(agentAttemptRepository).save(argThat(a ->
                a.getStatus() == AgentAttemptStatus.FAILED && "AGENT_UNAVAILABLE".equals(a.getFailureCode())));
        verify(aiOperationRepository).save(argThat(op -> op.getStatus() == AiOperationStatus.FAILED_RETRYABLE));
    }

    @Test
    void shouldMarkFailedRetryableOnAgentServerError() {
        AgentClientException agentEx = new AgentClientException(
                AgentClientException.Reason.AGENT_ERROR, "server error", new RuntimeException());
        when(assessmentAgentClient.generate(any(), anyString())).thenThrow(agentEx);

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1"))
                .isSameAs(agentEx);

        verify(agentAttemptRepository).save(argThat(a ->
                a.getStatus() == AgentAttemptStatus.FAILED && "AGENT_ERROR".equals(a.getFailureCode())));
        verify(aiOperationRepository).save(argThat(op -> op.getStatus() == AiOperationStatus.FAILED_RETRYABLE));
    }

    @Test
    void shouldMarkFailedTerminalOnAgentRejectedWithoutParsedDetail() {
        AgentClientException agentEx = new AgentClientException(
                AgentClientException.Reason.AGENT_REJECTED, "rejected", new RuntimeException());
        when(assessmentAgentClient.generate(any(), anyString())).thenThrow(agentEx);

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1"))
                .isSameAs(agentEx);

        verify(agentAttemptRepository).save(argThat(a ->
                a.getStatus() == AgentAttemptStatus.FAILED && "AGENT_REJECTED".equals(a.getFailureCode())));
        verify(aiOperationRepository).save(argThat(op -> op.getStatus() == AiOperationStatus.FAILED_TERMINAL));
    }

    @Test
    void shouldPersistVerbatimInvalidCommandFailureCodeAsTerminal() {
        AssessmentAgentErrorPayload agentError = new AssessmentAgentErrorPayload(
                "INVALID_COMMAND", "blank field", null, "agents-corr-1");
        AgentClientException agentEx = new AgentClientException(
                AgentClientException.Reason.AGENT_REJECTED, "rejected", new RuntimeException(), agentError);
        when(assessmentAgentClient.generate(any(), anyString())).thenThrow(agentEx);

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1"))
                .isSameAs(agentEx);

        verify(agentAttemptRepository).save(argThat(a ->
                a.getStatus() == AgentAttemptStatus.FAILED && "INVALID_COMMAND".equals(a.getFailureCode())));
        verify(aiOperationRepository).save(argThat(op -> op.getStatus() == AiOperationStatus.FAILED_TERMINAL));
    }

    @Test
    void shouldPersistVerbatimMalformedOutputFailureCodeAsRetryable() {
        AssessmentAgentErrorPayload agentError = new AssessmentAgentErrorPayload(
                "MALFORMED_OUTPUT", "bad output", null, "agents-corr-2");
        AgentClientException agentEx = new AgentClientException(
                AgentClientException.Reason.AGENT_REJECTED, "rejected", new RuntimeException(), agentError);
        when(assessmentAgentClient.generate(any(), anyString())).thenThrow(agentEx);

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1"))
                .isSameAs(agentEx);

        verify(agentAttemptRepository).save(argThat(a ->
                a.getStatus() == AgentAttemptStatus.FAILED && "MALFORMED_OUTPUT".equals(a.getFailureCode())));
        verify(aiOperationRepository).save(argThat(op -> op.getStatus() == AiOperationStatus.FAILED_RETRYABLE));
    }

    @Test
    void shouldPersistResolvedProviderAndModelWhenDispatchFailureOccursAfterProviderResolution() {
        // MALFORMED_OUTPUT means agents/ resolved a provider, called the LLM, and only then failed
        // to produce well-formed output — the log it returns alongside the error carries that
        // resolved provider/model, per Agents' documented nullability contract (see
        // API-A2-HANDOFF.md: "provider non-null on success or on a failure after provider
        // resolution (e.g. MALFORMED_OUTPUT)"). This must survive onto the persisted AgentAttempt,
        // not be discarded the way it was before this fix.
        AssessmentAgentResponse.Log logAfterResolution = new AssessmentAgentResponse.Log(UUID.randomUUID(),
                "assessment", "gemini", "gemini-2.0-flash", "v1", null, null, null, null, null,
                "FAILED", "MALFORMED_OUTPUT", Instant.now(), Instant.now());
        AssessmentAgentErrorPayload agentError = new AssessmentAgentErrorPayload(
                "MALFORMED_OUTPUT", "bad output", logAfterResolution, "agents-corr-3");
        AgentClientException agentEx = new AgentClientException(
                AgentClientException.Reason.AGENT_REJECTED, "rejected", new RuntimeException(), agentError);
        when(assessmentAgentClient.generate(any(), anyString())).thenThrow(agentEx);

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1"))
                .isSameAs(agentEx);

        verify(agentAttemptRepository).save(argThat(a ->
                a.getStatus() == AgentAttemptStatus.FAILED
                        && "MALFORMED_OUTPUT".equals(a.getFailureCode())
                        && "gemini".equals(a.getResolvedProvider())
                        && "gemini-2.0-flash".equals(a.getResolvedModel())));
    }

    @Test
    void shouldLeaveProviderAndModelNullWhenDispatchFailureOccursBeforeProviderResolution() {
        // UNREACHABLE is a transport-level failure — the call never reached agents/, so no
        // provider was ever resolved; null is the correct, honest value here, not a regression.
        AgentClientException agentEx = new AgentClientException(
                AgentClientException.Reason.UNREACHABLE, "unreachable", new RuntimeException());
        when(assessmentAgentClient.generate(any(), anyString())).thenThrow(agentEx);

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1"))
                .isSameAs(agentEx);

        verify(agentAttemptRepository).save(argThat(a ->
                a.getStatus() == AgentAttemptStatus.FAILED
                        && a.getResolvedProvider() == null && a.getResolvedModel() == null));
    }

    @Test
    void shouldPersistResolvedProviderAndModelOnStaleOnCompletionSinceTheAgentCallItselfSucceeded() {
        Assessment racedAssessment = assessment.withCurrentRevision(UUID.randomUUID());
        when(assessmentRepository.findById(assessment.getId())).thenReturn(Optional.of(racedAssessment));
        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(successResponse());

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1"))
                .isInstanceOf(StaleOnCompletionException.class);

        // successResponse()'s Log carries provider="gemini"/model="gemini-2.0-flash" — the agent
        // call itself succeeded; only the CAS write lost the race. That provider/model must not
        // be discarded just because no revision was created from the response.
        verify(agentAttemptRepository).save(argThat(a ->
                "STALE_ON_COMPLETION".equals(a.getFailureCode())
                        && "gemini".equals(a.getResolvedProvider())
                        && "gemini-2.0-flash".equals(a.getResolvedModel())));
    }

    @Test
    void shouldMarkStaleOnCompletionAndPreserveStructuredResultWhenCurrentRevisionMovedBeforePhase2() {
        Assessment racedAssessment = assessment.withCurrentRevision(UUID.randomUUID());
        when(assessmentRepository.findById(assessment.getId())).thenReturn(Optional.of(racedAssessment));
        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(successResponse());

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1"))
                .isInstanceOf(StaleOnCompletionException.class);

        verify(agentAttemptRepository).save(argThat(a ->
                a.getStatus() == AgentAttemptStatus.FAILED
                        && "STALE_ON_COMPLETION".equals(a.getFailureCode())
                        && a.getStructuredResult() != null
                        && a.getStructuredResult().contains("Title")));
        verify(aiOperationRepository).save(argThat(op -> op.getStatus() == AiOperationStatus.FAILED_TERMINAL));
        verify(assessmentRevisionRepository, never()).save(any());
    }

    @Test
    void shouldMarkStaleOnCompletionWhenOptimisticLockFailsAtCasWriteTime() {
        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(successResponse());
        doThrow(new ObjectOptimisticLockingFailureException(Assessment.class, assessment.getId().value().toString()))
                .when(assessmentRepository).save(argThat(a -> a.getCurrentRevisionId() != null));

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1"))
                .isInstanceOf(StaleOnCompletionException.class);

        verify(agentAttemptRepository).save(argThat(a ->
                a.getStatus() == AgentAttemptStatus.FAILED && "STALE_ON_COMPLETION".equals(a.getFailureCode())));
        verify(aiOperationRepository).save(argThat(op -> op.getStatus() == AiOperationStatus.FAILED_TERMINAL));
    }

    @Test
    void shouldTranslatePhase0InFlightCollisionToStaleRevisionForInitialGeneration() {
        // A3 Contract Correction § Correction 4: createInitialRevision/regenerateRevision always
        // insert a brand-new AiOperation row, so their only possible Phase 0 collision is
        // uq_ai_operations_in_flight — this must remain STALE_REVISION, never reinterpreted as
        // OPERATION_IN_PROGRESS just because that constraint-violation exception type is generic.
        doThrow(new DataIntegrityViolationException("duplicate in-flight ai_operations row"))
                .when(aiOperationRepository).save(any());

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1"))
                .isInstanceOf(StaleRevisionException.class);

        verifyNoInteractions(assessmentAgentClient);
    }

    @Test
    void shouldTranslatePhase0AgentAttemptCollisionToOperationInProgressForRetry() {
        AiOperation existingOperation = AiOperation.create(assessment.getId(),
                        cl.gradeops.ai.api.assessment.domain.model.AiOperationType.CREATE_INITIAL_REVISION,
                        "uid-1", "key-1", null)
                .markInProgress().markFailedRetryable();
        doThrow(new DataIntegrityViolationException("duplicate agent_attempts(ai_operation_id, attempt_number)"))
                .when(agentAttemptRepository).save(any());

        assertThatThrownBy(() -> coordinator.retryInitialRevision(assessment, existingOperation, agentCommand, 2))
                .isInstanceOf(OperationInProgressException.class);

        verifyNoInteractions(assessmentAgentClient);
    }

    @Test
    void shouldSupportLegacyAssessmentWithNullCurrentRevisionGoingThroughInitialGeneration() {
        Assessment legacyAssessment = Assessment.restore(
                new AssessmentId(UUID.randomUUID()), "uid-1",
                cl.gradeops.ai.api.assessment.domain.model.AssessmentStatus.DRAFT, Instant.now());
        when(assessmentRepository.findById(legacyAssessment.getId())).thenReturn(Optional.of(legacyAssessment));
        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(successResponse());

        AssessmentRevision revision = coordinator.createInitialRevision(legacyAssessment, agentCommand, "uid-1", "key-1");

        assertThat(revision).isNotNull();
        assertThat(revision.getVersionNumber()).isEqualTo(1);
        verify(assessmentRepository).save(argThat(a -> revision.getId().equals(a.getCurrentRevisionId())));
    }
}
