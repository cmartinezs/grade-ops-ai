package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AgentClientException;
import cl.gradeops.ai.api.agentclient.AssessmentAgentClient;
import cl.gradeops.ai.api.agentclient.AssessmentAgentResponse;
import cl.gradeops.ai.api.agentclient.AssessmentCommand;
import cl.gradeops.ai.api.assessment.application.exception.StaleOnCompletionException;
import cl.gradeops.ai.api.assessment.application.port.out.AgentAttemptRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AiOperationRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRevisionRepositoryPort;
import cl.gradeops.ai.api.assessment.domain.model.AgentAttempt;
import cl.gradeops.ai.api.assessment.domain.model.AiOperation;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationType;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Replaces this packet's previous shared agent-dispatch coordinator with the durable, three-phase
 * {@code AiOperation}/{@code AgentAttempt}/{@code AssessmentRevision} write path (Durable AI
 * Operation Model ADR). Durable evidence (the {@code AiOperation}/{@code AgentAttempt} pair) is
 * committed in Phase 0, in its own transaction, <strong>before</strong> the HTTP call to
 * {@code agents/} — the entire point of this class: a crash between provider success and API
 * persistence must never lose all evidence the call ever happened.
 *
 * <p>Scoped to {@code CREATE_INITIAL_REVISION} only this session — {@code
 * expectedRevisionId}/{@code STALE_REVISION} pre-dispatch checks belong to regenerate (Task 09,
 * a later session), not this coordinator's only caller today.
 *
 * <p>Phase 2's CAS write is a single transaction on the happy path. A true concurrent race can
 * only be detected by Hibernate's optimistic lock at flush/commit time, which happens after this
 * method's transactional callback has already returned — so a lost race surfaces as a thrown
 * {@link ObjectOptimisticLockingFailureException} from {@code transactionTemplate.execute(...)}
 * itself (Phase 2's insert/update work already rolled back), and the {@code
 * STALE_ON_COMPLETION} evidence is then recorded in a small follow-up transaction. This is the
 * only way to safely branch on a JPA optimistic-lock failure without leaving the persistence
 * context in an inconsistent mid-flush state.
 */
public class AiOperationCoordinator {

    private final AssessmentRepositoryPort assessmentRepository;
    private final AiOperationRepositoryPort aiOperationRepository;
    private final AgentAttemptRepositoryPort agentAttemptRepository;
    private final AssessmentRevisionRepositoryPort assessmentRevisionRepository;
    private final AssessmentAgentClient assessmentAgentClient;
    private final JsonMapper jsonMapper;
    private final TransactionTemplate transactionTemplate;

    public AiOperationCoordinator(AssessmentRepositoryPort assessmentRepository,
                                   AiOperationRepositoryPort aiOperationRepository,
                                   AgentAttemptRepositoryPort agentAttemptRepository,
                                   AssessmentRevisionRepositoryPort assessmentRevisionRepository,
                                   AssessmentAgentClient assessmentAgentClient,
                                   JsonMapper jsonMapper,
                                   PlatformTransactionManager transactionManager) {
        this.assessmentRepository = assessmentRepository;
        this.aiOperationRepository = aiOperationRepository;
        this.agentAttemptRepository = agentAttemptRepository;
        this.assessmentRevisionRepository = assessmentRevisionRepository;
        this.assessmentAgentClient = assessmentAgentClient;
        this.jsonMapper = jsonMapper;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * Caller (the handler) has already verified ownership and the idempotency/{@code
     * ALREADY_GENERATED} preconditions. {@code assessment} may be stale by Phase 2 (the HTTP
     * call happens in between) — Phase 2 re-reads it fresh inside its own transaction.
     */
    public AssessmentRevision createInitialRevision(Assessment assessment, AssessmentCommand agentCommand,
                                                     String requestedBy, String idempotencyKey) {
        String correlationId = UUID.randomUUID().toString();
        AtomicReference<AiOperation> operationRef = new AtomicReference<>();
        AtomicReference<AgentAttempt> attemptRef = new AtomicReference<>();

        transactionTemplate.executeWithoutResult(status -> {
            AiOperation operation = AiOperation.create(assessment.getId(), AiOperationType.CREATE_INITIAL_REVISION,
                    requestedBy, idempotencyKey, null).markInProgress();
            aiOperationRepository.save(operation);

            AgentAttempt attempt = AgentAttempt.dispatch(operation.getId(), 1, "assessment", null, correlationId);
            agentAttemptRepository.save(attempt);

            operationRef.set(operation);
            attemptRef.set(attempt);
        });

        AiOperation operation = operationRef.get();
        AgentAttempt attempt = attemptRef.get();

        AssessmentAgentResponse response;
        try {
            response = assessmentAgentClient.generate(agentCommand, correlationId);
        } catch (AgentClientException ex) {
            persistDispatchFailure(operation, attempt, ex);
            throw ex;
        }

        return runPhase2(assessment, operation, attempt, response, requestedBy);
    }

    private AssessmentRevision runPhase2(Assessment assessment, AiOperation operation, AgentAttempt attempt,
                                          AssessmentAgentResponse response, String requestedBy) {
        String structuredResultJson = jsonMapper.writeValueAsString(response.result());
        AtomicBoolean staleAtReadTime = new AtomicBoolean(false);
        AtomicReference<AssessmentRevision> revisionRef = new AtomicReference<>();

        try {
            transactionTemplate.executeWithoutResult(status -> {
                Assessment fresh = assessmentRepository.findById(assessment.getId())
                        .orElseThrow(() -> new ResourceNotFoundException(assessment.getId().value().toString()));
                if (fresh.getCurrentRevisionId() != null) {
                    staleAtReadTime.set(true);
                    status.setRollbackOnly();
                    return;
                }

                AssessmentRevision revision = AssessmentRevision.generateFromAi(assessment.getId(),
                        response.result().title(), response.result().context(), response.result().instructions(),
                        response.result().objectives(), response.result().deliverables(), response.result().constraints(),
                        requestedBy, attempt.getId());
                assessmentRevisionRepository.save(revision);

                AgentAttempt completed = attempt.markCompleted(response.log().provider(), response.log().model(), null,
                        response.log().estimatedInputTokens(), response.log().estimatedOutputTokens(),
                        toBigDecimal(response.log().costEstimate()), structuredResultJson);
                agentAttemptRepository.save(completed);

                AiOperation succeeded = operation.markSucceeded(revision.getId());
                aiOperationRepository.save(succeeded);

                assessmentRepository.save(fresh.withCurrentRevision(revision.getId()));

                revisionRef.set(revision);
            });
        } catch (ObjectOptimisticLockingFailureException ex) {
            persistStaleOnCompletion(operation, attempt, structuredResultJson);
            throw new StaleOnCompletionException(assessment.getId().value().toString());
        }

        if (staleAtReadTime.get()) {
            persistStaleOnCompletion(operation, attempt, structuredResultJson);
            throw new StaleOnCompletionException(assessment.getId().value().toString());
        }

        return revisionRef.get();
    }

    private void persistStaleOnCompletion(AiOperation operation, AgentAttempt attempt, String structuredResultJson) {
        transactionTemplate.executeWithoutResult(status -> {
            agentAttemptRepository.save(attempt.markFailed("STALE_ON_COMPLETION", structuredResultJson));
            aiOperationRepository.save(operation.markFailedTerminal());
        });
    }

    private void persistDispatchFailure(AiOperation operation, AgentAttempt attempt, AgentClientException ex) {
        String failureCode = mapFailureCode(ex);
        boolean retryable = isRetryable(failureCode);
        transactionTemplate.executeWithoutResult(status -> {
            agentAttemptRepository.save(attempt.markFailed(failureCode, null));
            AiOperation failed = retryable ? operation.markFailedRetryable() : operation.markFailedTerminal();
            aiOperationRepository.save(failed);
        });
    }

    /** Per LOCAL-CONTRACTS.md § Canonical failure-code taxonomy — no generic synonyms. */
    private static String mapFailureCode(AgentClientException ex) {
        if (ex.reason() == AgentClientException.Reason.UNREACHABLE) {
            return "AGENT_UNAVAILABLE";
        }
        if (ex.reason() == AgentClientException.Reason.AGENT_ERROR) {
            return "AGENT_ERROR";
        }
        // AGENT_REJECTED (agents/ 4xx) — read the parsed agents/-side detail when available,
        // rather than only inspecting the HTTP status class.
        if (ex.agentError() != null) {
            String agentsCode = ex.agentError().errorCode();
            if ("INVALID_COMMAND".equals(agentsCode) || "MALFORMED_OUTPUT".equals(agentsCode)) {
                return agentsCode;
            }
        }
        return "AGENT_REJECTED";
    }

    private static boolean isRetryable(String failureCode) {
        return switch (failureCode) {
            case "AGENT_UNAVAILABLE", "AGENT_ERROR", "MALFORMED_OUTPUT" -> true;
            case "AGENT_REJECTED", "INVALID_COMMAND" -> false;
            default -> false;
        };
    }

    private static BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }
}
