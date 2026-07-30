package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AgentClientException;
import cl.gradeops.ai.api.agentclient.AssessmentAgentClient;
import cl.gradeops.ai.api.agentclient.AssessmentAgentResponse;
import cl.gradeops.ai.api.agentclient.AssessmentCommand;
import cl.gradeops.ai.api.assessment.application.exception.StaleOnCompletionException;
import cl.gradeops.ai.api.assessment.application.exception.StaleRevisionException;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * The durable, three-phase {@code AiOperation}/{@code AgentAttempt}/{@code AssessmentRevision}
 * write path (Durable AI Operation Model ADR) shared by every AI-dispatch operation this packet
 * defines: initial generation ({@link #createInitialRevision}, Task 07B/Session A2) and
 * regenerate ({@link #regenerateRevision}, Task 09/Session A3). Durable evidence (the {@code
 * AiOperation}/{@code AgentAttempt} pair) is committed in Phase 0, in its own transaction,
 * <strong>before</strong> the HTTP call to {@code agents/} — the entire point of this class: a
 * crash between provider success and API persistence must never lose all evidence the call ever
 * happened.
 *
 * <p>The two callers differ only in: what {@code AiOperationType}/{@code expectedRevisionId} the
 * durable {@code AiOperation} record carries, what counts as "stale" at Phase 2 (no current
 * revision yet, vs. current revision no longer matching what was expected), and how the
 * resulting {@link AssessmentRevision} is built (first version vs. chained onto a specific
 * predecessor). {@link #dispatchAndPersist} captures everything else exactly once.
 *
 * <p>Phase 0's own insert can also lose a race: {@code uq_ai_operations_in_flight} rejects a
 * second {@code PENDING}/{@code IN_PROGRESS} {@code AiOperation} row for the same {@code
 * (assessmentId, operationType)}, so two truly concurrent dispatches for the same assessment (a
 * real scenario for regenerate, where two distinct idempotency keys legitimately race) surface as
 * a caught {@code DataIntegrityViolationException}, reported as {@code STALE_REVISION} — no
 * partial evidence survives for the loser since the whole Phase 0 transaction rolls back.
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
        AiOperation operation = AiOperation.create(assessment.getId(), AiOperationType.CREATE_INITIAL_REVISION,
                requestedBy, idempotencyKey, null).markInProgress();

        return dispatchAndPersist(assessment, operation, 1, agentCommand,
                fresh -> fresh.getCurrentRevisionId() != null,
                (attempt, response) -> AssessmentRevision.generateFromAi(assessment.getId(),
                        response.result().title(), response.result().context(), response.result().instructions(),
                        response.result().objectives(), response.result().deliverables(), response.result().constraints(),
                        requestedBy, attempt.getId()));
    }

    /**
     * Caller (the handler) has already verified ownership, idempotency, and the pre-dispatch
     * {@code expectedRevisionId} staleness check (a mismatch there must reject with {@code
     * STALE_REVISION} before ever reaching this method — this method's own staleness check is
     * the CAS-at-persist-time backstop for the residual race, not the primary check).
     */
    public AssessmentRevision regenerateRevision(Assessment assessment, AssessmentRevision expectedRevision,
                                                  AssessmentCommand agentCommand, String requestedBy, String reason,
                                                  String idempotencyKey) {
        UUID expectedRevisionId = expectedRevision.getId();
        AiOperation operation = AiOperation.create(assessment.getId(), AiOperationType.REGENERATE_REVISION,
                requestedBy, idempotencyKey, expectedRevisionId).markInProgress();

        return dispatchAndPersist(assessment, operation, 1, agentCommand,
                fresh -> !expectedRevisionId.equals(fresh.getCurrentRevisionId()),
                (attempt, response) -> AssessmentRevision.regenerateFromAi(expectedRevision,
                        response.result().title(), response.result().context(), response.result().instructions(),
                        response.result().objectives(), response.result().deliverables(), response.result().constraints(),
                        requestedBy, reason, attempt.getId()));
    }

    /**
     * Dispatches a fresh {@link AgentAttempt} under an already-transitioned-to-{@code
     * IN_PROGRESS} {@code AiOperation} — the caller decides whether that operation is brand new
     * ({@link #createInitialRevision}/{@link #regenerateRevision}) or an existing one being
     * retried, and what {@code attemptNumber} this dispatch is. {@code operation} is not yet
     * saved; Phase 0 persists it alongside the new attempt in one transaction.
     */
    private AssessmentRevision dispatchAndPersist(Assessment assessment, AiOperation operation, int attemptNumber,
            AssessmentCommand agentCommand, Predicate<Assessment> staleAtCompletion,
            BiFunction<AgentAttempt, AssessmentAgentResponse, AssessmentRevision> revisionFactory) {
        String correlationId = UUID.randomUUID().toString();
        AtomicReference<AgentAttempt> attemptRef = new AtomicReference<>();

        try {
            transactionTemplate.executeWithoutResult(status -> {
                aiOperationRepository.save(operation);

                AgentAttempt attempt = AgentAttempt.dispatch(operation.getId(), attemptNumber, "assessment", null, correlationId);
                agentAttemptRepository.save(attempt);

                attemptRef.set(attempt);
            });
        } catch (DataIntegrityViolationException ex) {
            // Two truly concurrent dispatches for the same (assessmentId, operationType) both
            // reach Phase 0 before either commits — uq_ai_operations_in_flight is the DB-level
            // backstop for exactly this (Idempotency and Concurrency Strategy ADR), narrowing but
            // not eliminating the residual pre-check-to-commit race. No AiOperation/AgentAttempt
            // row survives for the loser (the whole Phase 0 transaction rolled back), no agents/
            // call is ever made for it, so this is reported identically to a pre-dispatch
            // STALE_REVISION conflict, not a new failure mode.
            throw new StaleRevisionException(assessment.getId().value().toString());
        }

        AgentAttempt attempt = attemptRef.get();

        AssessmentAgentResponse response;
        try {
            response = assessmentAgentClient.generate(agentCommand, correlationId);
        } catch (AgentClientException ex) {
            persistDispatchFailure(operation, attempt, ex);
            throw ex;
        }

        return runPhase2(assessment, operation, attempt, response, staleAtCompletion, revisionFactory);
    }

    private AssessmentRevision runPhase2(Assessment assessment, AiOperation operation, AgentAttempt attempt,
            AssessmentAgentResponse response, Predicate<Assessment> staleAtCompletion,
            BiFunction<AgentAttempt, AssessmentAgentResponse, AssessmentRevision> revisionFactory) {
        String structuredResultJson = jsonMapper.writeValueAsString(response.result());
        AtomicBoolean staleAtReadTime = new AtomicBoolean(false);
        AtomicReference<AssessmentRevision> revisionRef = new AtomicReference<>();

        try {
            transactionTemplate.executeWithoutResult(status -> {
                Assessment fresh = assessmentRepository.findById(assessment.getId())
                        .orElseThrow(() -> new ResourceNotFoundException(assessment.getId().value().toString()));
                if (staleAtCompletion.test(fresh)) {
                    staleAtReadTime.set(true);
                    status.setRollbackOnly();
                    return;
                }

                AssessmentRevision revision = revisionFactory.apply(attempt, response);
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
            persistStaleOnCompletion(operation, attempt, response, structuredResultJson);
            throw new StaleOnCompletionException(assessment.getId().value().toString());
        }

        if (staleAtReadTime.get()) {
            persistStaleOnCompletion(operation, attempt, response, structuredResultJson);
            throw new StaleOnCompletionException(assessment.getId().value().toString());
        }

        return revisionRef.get();
    }

    /**
     * The agent call itself succeeded (a response exists) — only the CAS write lost the race —
     * so {@code response.log().provider()}/{@code .model()} are always non-null here (the same
     * nullability contract as a completed attempt) and must be preserved, not discarded just
     * because no revision was created from this response.
     */
    private void persistStaleOnCompletion(AiOperation operation, AgentAttempt attempt, AssessmentAgentResponse response,
                                           String structuredResultJson) {
        transactionTemplate.executeWithoutResult(status -> {
            agentAttemptRepository.save(attempt.markFailed("STALE_ON_COMPLETION",
                    response.log().provider(), response.log().model(), structuredResultJson));
            aiOperationRepository.save(operation.markFailedTerminal());
        });
    }

    private void persistDispatchFailure(AiOperation operation, AgentAttempt attempt, AgentClientException ex) {
        String failureCode = mapFailureCode(ex);
        boolean retryable = isRetryable(failureCode);
        String resolvedProvider = resolvedProviderFrom(ex);
        String resolvedModel = resolvedModelFrom(ex);
        transactionTemplate.executeWithoutResult(status -> {
            agentAttemptRepository.save(attempt.markFailed(failureCode, resolvedProvider, resolvedModel, null));
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

    /**
     * Non-null only when agents/ returned a parseable error body whose {@code log} carries a
     * resolved provider — i.e. the failure happened after provider resolution (MALFORMED_OUTPUT).
     * Null for transport-level failures (UNREACHABLE/AGENT_ERROR, no response body at all) and
     * for pre-resolution agents-side rejections (INVALID_COMMAND).
     */
    private static String resolvedProviderFrom(AgentClientException ex) {
        return ex.agentError() != null && ex.agentError().log() != null ? ex.agentError().log().provider() : null;
    }

    private static String resolvedModelFrom(AgentClientException ex) {
        return ex.agentError() != null && ex.agentError().log() != null ? ex.agentError().log().model() : null;
    }

    private static BigDecimal toBigDecimal(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }
}
