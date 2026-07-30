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
import cl.gradeops.ai.api.assessment.domain.model.AiOperationType;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyCompletionContext;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyGuard;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyScope;
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
    @Mock IdempotencyGuard idempotencyGuard;
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
                assessmentRevisionRepository, assessmentAgentClient, idempotencyGuard, jsonMapper, transactionManager);
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

        coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1", null);

        InOrder inOrder = inOrder(aiOperationRepository, agentAttemptRepository, assessmentAgentClient);
        inOrder.verify(aiOperationRepository).save(argThat(op -> op.getStatus() == AiOperationStatus.IN_PROGRESS));
        inOrder.verify(agentAttemptRepository).save(argThat(a -> a.getStatus() == AgentAttemptStatus.DISPATCHED));
        inOrder.verify(assessmentAgentClient).generate(any(), anyString());
    }

    @Test
    void shouldNotOpenAnyTransactionDuringTheHttpCall() {
        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(successResponse());

        coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1", null);

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

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1", null))
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

        AssessmentRevision revision = coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1", null);

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

        coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1", null);

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

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1", null))
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

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1", null))
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

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1", null))
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

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1", null))
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

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1", null))
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

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1", null))
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

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1", null))
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

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1", null))
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

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1", null))
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

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1", null))
                .isInstanceOf(StaleOnCompletionException.class);

        verify(agentAttemptRepository).save(argThat(a ->
                a.getStatus() == AgentAttemptStatus.FAILED && "STALE_ON_COMPLETION".equals(a.getFailureCode())));
        verify(aiOperationRepository).save(argThat(op -> op.getStatus() == AiOperationStatus.FAILED_TERMINAL));
    }

    @Test
    void shouldTranslatePhase0InFlightCollisionToStaleRevisionForInitialGeneration() {
        // A3 Contract Correction § Correction 4 / A3 Final Idempotency Correction § 6: a DIFFERENT
        // idempotency key colliding on uq_ai_operations_in_flight is a genuine distinct-request
        // race, not a duplicate submission of the same logical request — this must remain
        // STALE_REVISION, never reinterpreted as OPERATION_IN_PROGRESS just because the
        // constraint-violation exception type is generic.
        AiOperation winner = AiOperation.create(assessment.getId(), AiOperationType.CREATE_INITIAL_REVISION,
                "uid-1", "different-key", null).markInProgress();
        when(aiOperationRepository.findLatestByAssessmentIdAndOperationType(assessment.getId(), AiOperationType.CREATE_INITIAL_REVISION))
                .thenReturn(Optional.of(winner));
        doThrow(new DataIntegrityViolationException("duplicate in-flight ai_operations row"))
                .when(aiOperationRepository).save(any());

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1", null))
                .isInstanceOf(StaleRevisionException.class);

        verifyNoInteractions(assessmentAgentClient);
    }

    @Test
    void shouldTranslatePhase0InFlightCollisionToOperationInProgressWhenTheWinnerHasTheSameIdempotencyKey() {
        // A3 Final Idempotency Correction § 6: the SAME idempotency key colliding on
        // uq_ai_operations_in_flight means the same logical request arrived twice (a genuine
        // concurrent duplicate submission) — not a real conflict. This must never surface as
        // STALE_REVISION; the caller relays the winner's in-flight/completed state instead.
        AiOperation winner = AiOperation.create(assessment.getId(), AiOperationType.CREATE_INITIAL_REVISION,
                "uid-1", "key-1", null).markInProgress();
        when(aiOperationRepository.findLatestByAssessmentIdAndOperationType(assessment.getId(), AiOperationType.CREATE_INITIAL_REVISION))
                .thenReturn(Optional.of(winner));
        doThrow(new DataIntegrityViolationException("duplicate in-flight ai_operations row"))
                .when(aiOperationRepository).save(any());

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1", null))
                .isInstanceOf(OperationInProgressException.class);

        verifyNoInteractions(assessmentAgentClient);
    }

    @Test
    void shouldRecordIdempotencyCompletionInTheSameTransactionAsTheSuccessfulOutcome() {
        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(successResponse());
        IdempotencyCompletionContext context = new IdempotencyCompletionContext(
                IdempotencyScope.assessment(assessment.getId().value()), "CREATE_INITIAL_REVISION", "key-1", "hash-1");

        AssessmentRevision revision =
                coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1", context);

        InOrder inOrder = inOrder(assessmentRevisionRepository, aiOperationRepository, assessmentRepository, idempotencyGuard);
        inOrder.verify(assessmentRevisionRepository).save(revision);
        inOrder.verify(aiOperationRepository).save(argThat(op -> op.getStatus() == AiOperationStatus.SUCCEEDED));
        inOrder.verify(assessmentRepository).save(argThat(a -> revision.getId().equals(a.getCurrentRevisionId())));
        inOrder.verify(idempotencyGuard).record(context.scope(), context.operationType(), context.idempotencyKey(),
                context.requestPayloadHash(), revision.getId().toString(), 201);
    }

    @Test
    void shouldNotReturnASuccessfulRevisionWhenTheIdempotencyRecordWriteFailsInTheSameTransaction() {
        // A3 Final Idempotency Correction § 7 "Atomicidad de éxito": a failure writing the
        // IdempotencyRecord must not be swallowed into a fake success — in a real transaction the
        // revision/attempt/operation/CAS writes issued in the same callback roll back together.
        when(assessmentAgentClient.generate(any(), anyString())).thenReturn(successResponse());
        IdempotencyCompletionContext context = new IdempotencyCompletionContext(
                IdempotencyScope.assessment(assessment.getId().value()), "CREATE_INITIAL_REVISION", "key-1", "hash-1");
        doThrow(new DataIntegrityViolationException("duplicate idempotency key"))
                .when(idempotencyGuard).record(any(), anyString(), anyString(), anyString(), anyString(), anyInt());

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1", context))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldRecordIdempotencyCompletionAs202InTheSameTransactionAsADispatchFailureForInitialGeneration() {
        AgentClientException agentEx = new AgentClientException(
                AgentClientException.Reason.UNREACHABLE, "unreachable", new RuntimeException());
        when(assessmentAgentClient.generate(any(), anyString())).thenThrow(agentEx);
        IdempotencyCompletionContext context = new IdempotencyCompletionContext(
                IdempotencyScope.assessment(assessment.getId().value()), "CREATE_INITIAL_REVISION", "key-1", "hash-1");

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1", context))
                .isSameAs(agentEx);

        ArgumentCaptor<AiOperation> operationCaptor = ArgumentCaptor.forClass(AiOperation.class);
        verify(aiOperationRepository, times(2)).save(operationCaptor.capture());
        UUID operationId = operationCaptor.getAllValues().get(1).getId();
        verify(idempotencyGuard).record(context.scope(), context.operationType(), context.idempotencyKey(),
                context.requestPayloadHash(), operationId.toString(), 202);
    }

    @Test
    void shouldNotSwallowAnIdempotencyRecordWriteFailureDuringDispatchFailurePersistence() {
        // A3 Final Idempotency Correction § 7 "Atomicidad de fallo": if the IdempotencyRecord
        // write fails while persisting a durable dispatch failure, the original AgentClientException
        // must not silently surface as a clean 202 with no record — Phase 0 evidence remains
        // recoverable, generation-status can project INDETERMINATE.
        AgentClientException agentEx = new AgentClientException(
                AgentClientException.Reason.UNREACHABLE, "unreachable", new RuntimeException());
        when(assessmentAgentClient.generate(any(), anyString())).thenThrow(agentEx);
        IdempotencyCompletionContext context = new IdempotencyCompletionContext(
                IdempotencyScope.assessment(assessment.getId().value()), "CREATE_INITIAL_REVISION", "key-1", "hash-1");
        doThrow(new DataIntegrityViolationException("duplicate idempotency key"))
                .when(idempotencyGuard).record(any(), anyString(), anyString(), anyString(), anyString(), anyInt());

        assertThatThrownBy(() -> coordinator.createInitialRevision(assessment, agentCommand, "uid-1", "key-1", context))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shouldRecordTheDerivedHttpStatusNotTwoOhTwoWhenRegenerateDispatchFails() {
        // Regenerate never returns 202 — a durable dispatch failure must be recorded under the
        // real HTTP status agents/'s failure reason maps to, so a replay reproduces that same
        // status (A3 Final Idempotency Correction § 3.4), not initial generation's fixed 202.
        AssessmentRevision expectedRevision = AssessmentRevision.generateFromAi(assessment.getId(), "T", "C", "I",
                List.of("o"), List.of("d"), List.of("c"), "uid-1", UUID.randomUUID());
        AgentClientException agentEx = new AgentClientException(
                AgentClientException.Reason.UNREACHABLE, "unreachable", new RuntimeException());
        when(assessmentAgentClient.generate(any(), anyString())).thenThrow(agentEx);
        IdempotencyCompletionContext context = new IdempotencyCompletionContext(
                IdempotencyScope.assessment(assessment.getId().value()), "REGENERATE_REVISION", "key-1", "hash-1");

        assertThatThrownBy(() -> coordinator.regenerateRevision(assessment, expectedRevision, agentCommand, "uid-1",
                "reason", "key-1", context))
                .isSameAs(agentEx);

        verify(idempotencyGuard).record(eq(context.scope()), eq("REGENERATE_REVISION"), eq("key-1"), eq("hash-1"),
                anyString(), eq(503));
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

        AssessmentRevision revision = coordinator.createInitialRevision(legacyAssessment, agentCommand, "uid-1", "key-1", null);

        assertThat(revision).isNotNull();
        assertThat(revision.getVersionNumber()).isEqualTo(1);
        verify(assessmentRepository).save(argThat(a -> revision.getId().equals(a.getCurrentRevisionId())));
    }
}
