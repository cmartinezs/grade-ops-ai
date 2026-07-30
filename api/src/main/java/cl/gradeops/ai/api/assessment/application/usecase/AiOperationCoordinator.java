package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AgentClientException;
import cl.gradeops.ai.api.agentclient.AssessmentAgentClient;
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
import cl.gradeops.ai.api.assessment.domain.model.AiOperation;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationType;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyCompletionContext;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyGuard;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The durable, three-phase {@code AiOperation}/{@code AgentAttempt}/{@code AssessmentRevision}
 * write path (Durable AI Operation Model ADR) shared by every AI-dispatch operation this packet
 * defines: initial generation ({@link #createInitialRevision}, Task 07B/Session A2), regenerate
 * ({@link #regenerateRevision}, Task 09/Session A3), and retry ({@link #retryInitialRevision},
 * Task 10). Durable evidence (the {@code AiOperation}/{@code AgentAttempt} pair) is committed in
 * Phase 0, in its own transaction, <strong>before</strong> the HTTP call to {@code agents/} — the
 * entire point of this class: a crash between provider success and API persistence must never
 * lose all evidence the call ever happened.
 *
 * <p>A3 Final Idempotency Correction: the {@link IdempotencyCompletionContext} a caller supplies
 * (non-null for {@link #createInitialRevision}/{@link #regenerateRevision}, {@code null} for
 * {@link #retryInitialRevision} — retry never carries its own idempotency key) is recorded as an
 * {@code IdempotencyRecord} <strong>inside the very same Phase 2 transaction</strong> that
 * produces the durable outcome it describes (success, dispatch failure, or stale-on-completion) —
 * never in a separate transaction afterward. This closes the crash window where Phase 2 could
 * commit a functional result while the process died before a caller-side {@code
 * idempotencyGuard.record(...)} call ran, leaving a replay unrecognizable and risking a second,
 * paid LLM dispatch for what should have been a no-op replay.
 *
 * <p>The two AI-dispatch callers differ only in: what {@code AiOperationType}/{@code
 * expectedRevisionId} the durable {@code AiOperation} record carries, what counts as "stale" at
 * Phase 2 (no current revision yet, vs. current revision no longer matching what was expected),
 * how the resulting {@link AssessmentRevision} is built (first version vs. chained onto a specific
 * predecessor), and what HTTP status a durable failure is recorded/replayed under (initial
 * generation always {@code 202}; regenerate derives the original status from the failure code via
 * {@link RegenerateFailureReplay}, since it never returns {@code 202}). {@link #dispatchAndPersist}
 * captures everything else exactly once.
 *
 * <p>Phase 0's own insert can also lose a race: {@code uq_ai_operations_in_flight} rejects a
 * second {@code PENDING}/{@code IN_PROGRESS} {@code AiOperation} row for the same {@code
 * (assessmentId, operationType)}. {@link #resolvePhase0Conflict} distinguishes two structurally
 * different reasons this can happen: the winner holds the <em>same</em> idempotency key as the
 * loser (a genuine duplicate submission of the same logical request — never a real conflict, so
 * it is reported as {@link OperationInProgressException}, letting the caller relay the winner's
 * in-flight/completed state instead of a spurious staleness error) or a <em>different</em> key
 * (two distinct, legitimately racing requests — reported via the caller-supplied {@code
 * phase0ConflictException}, unchanged from before this correction).
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
    private final IdempotencyGuard idempotencyGuard;
    private final JsonMapper jsonMapper;
    private final TransactionTemplate transactionTemplate;

    public AiOperationCoordinator(AssessmentRepositoryPort assessmentRepository,
                                   AiOperationRepositoryPort aiOperationRepository,
                                   AgentAttemptRepositoryPort agentAttemptRepository,
                                   AssessmentRevisionRepositoryPort assessmentRevisionRepository,
                                   AssessmentAgentClient assessmentAgentClient,
                                   IdempotencyGuard idempotencyGuard,
                                   JsonMapper jsonMapper,
                                   PlatformTransactionManager transactionManager) {
        this.assessmentRepository = assessmentRepository;
        this.aiOperationRepository = aiOperationRepository;
        this.agentAttemptRepository = agentAttemptRepository;
        this.assessmentRevisionRepository = assessmentRevisionRepository;
        this.assessmentAgentClient = assessmentAgentClient;
        this.idempotencyGuard = idempotencyGuard;
        this.jsonMapper = jsonMapper;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * Caller (the handler) has already verified ownership and the idempotency/{@code
     * ALREADY_GENERATED} preconditions. {@code assessment} may be stale by Phase 2 (the HTTP
     * call happens in between) — Phase 2 re-reads it fresh inside its own transaction.
     */
    public AssessmentRevision createInitialRevision(Assessment assessment, AssessmentCommand agentCommand,
                                                     String requestedBy, String idempotencyKey,
                                                     IdempotencyCompletionContext idempotencyContext) {
        AiOperation operation = AiOperation.create(assessment.getId(), AiOperationType.CREATE_INITIAL_REVISION,
                requestedBy, idempotencyKey, null).markInProgress();

        return dispatchAndPersist(assessment, operation, 1, agentCommand,
                fresh -> fresh.getCurrentRevisionId() != null,
                (attempt, response) -> AssessmentRevision.generateFromAi(assessment.getId(),
                        response.result().title(), response.result().context(), response.result().instructions(),
                        response.result().objectives(), response.result().deliverables(), response.result().constraints(),
                        requestedBy, attempt.getId()),
                StaleRevisionException::new, idempotencyContext);
    }

    /**
     * Caller (the handler) has already verified ownership, idempotency, and the pre-dispatch
     * {@code expectedRevisionId} staleness check (a mismatch there must reject with {@code
     * STALE_REVISION} before ever reaching this method — this method's own staleness check is
     * the CAS-at-persist-time backstop for the residual race, not the primary check).
     */
    public AssessmentRevision regenerateRevision(Assessment assessment, AssessmentRevision expectedRevision,
                                                  AssessmentCommand agentCommand, String requestedBy, String reason,
                                                  String idempotencyKey, IdempotencyCompletionContext idempotencyContext) {
        UUID expectedRevisionId = expectedRevision.getId();
        AiOperation operation = AiOperation.create(assessment.getId(), AiOperationType.REGENERATE_REVISION,
                requestedBy, idempotencyKey, expectedRevisionId).markInProgress();

        return dispatchAndPersist(assessment, operation, 1, agentCommand,
                fresh -> !expectedRevisionId.equals(fresh.getCurrentRevisionId()),
                (attempt, response) -> AssessmentRevision.regenerateFromAi(expectedRevision,
                        response.result().title(), response.result().context(), response.result().instructions(),
                        response.result().objectives(), response.result().deliverables(), response.result().constraints(),
                        requestedBy, reason, attempt.getId()),
                StaleRevisionException::new, idempotencyContext);
    }

    /**
     * Task 10 / retry: dispatches a new {@link AgentAttempt} under the SAME, already-existing
     * {@code AiOperation} — never a new operation, never a new {@code Assessment} ("retries
     * quedan asociados a la misma operación lógica"). Caller ({@link RetryGenerationHandler}) has
     * already verified ownership and the retryability preconditions (an active FAILED_RETRYABLE
     * operation, or a PENDING/IN_PROGRESS one past the indeterminate threshold). Scoped to {@code
     * CREATE_INITIAL_REVISION} only — see LOCAL-CONTRACTS.md § API ↔ Web public contract; the
     * retry endpoint's resume story is specifically about the initial-generation dead-end
     * (Research 02 §5.6), not regenerate, which never leaves the assessment revision-less. Retry
     * has no {@code Idempotency-Key} of its own, so no {@link IdempotencyCompletionContext} is
     * ever recorded for it — {@code null} is passed through unconditionally.
     */
    public AssessmentRevision retryInitialRevision(Assessment assessment, AiOperation existingOperation,
                                                    AssessmentCommand agentCommand, int nextAttemptNumber) {
        AiOperation operation = existingOperation.markInProgress();
        String requestedBy = existingOperation.getRequestedBy();

        return dispatchAndPersist(assessment, operation, nextAttemptNumber, agentCommand,
                fresh -> fresh.getCurrentRevisionId() != null,
                (attempt, response) -> AssessmentRevision.generateFromAi(assessment.getId(),
                        response.result().title(), response.result().context(), response.result().instructions(),
                        response.result().objectives(), response.result().deliverables(), response.result().constraints(),
                        requestedBy, attempt.getId()),
                OperationInProgressException::new, null);
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
            BiFunction<AgentAttempt, AssessmentAgentResponse, AssessmentRevision> revisionFactory,
            Function<String, RuntimeException> phase0ConflictException,
            IdempotencyCompletionContext idempotencyContext) {
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
            // Two truly concurrent dispatches reach Phase 0 before either commits — see
            // resolvePhase0Conflict's javadoc for how a same-key collision (no real conflict) is
            // told apart from a different-key one (a genuine race). No AiOperation/AgentAttempt
            // row survives for the loser (the whole Phase 0 transaction rolled back); no agents/
            // call is ever made for it.
            throw resolvePhase0Conflict(assessment, operation, phase0ConflictException);
        }

        AgentAttempt attempt = attemptRef.get();

        AssessmentAgentResponse response;
        try {
            response = assessmentAgentClient.generate(agentCommand, correlationId);
        } catch (AgentClientException ex) {
            persistDispatchFailure(operation, attempt, ex, idempotencyContext);
            throw ex;
        }

        return runPhase2(assessment, operation, attempt, response, staleAtCompletion, revisionFactory, idempotencyContext);
    }

    /**
     * A Phase-0 unique-constraint collision on {@code (assessmentId, operationType)} always means
     * exactly one other {@code AiOperation} is currently {@code PENDING}/{@code IN_PROGRESS} for
     * this pair — {@code findLatestByAssessmentIdAndOperationType} therefore finds that winner
     * (our own insert never committed). If it carries the SAME idempotency key as the request that
     * just lost the race, this is not a conflict at all — it is the same logical request arriving
     * twice (a genuine concurrent duplicate submission, or a client retry racing its own earlier
     * attempt) — so it is reported as {@link OperationInProgressException}, letting the caller
     * relay the winner's current (possibly by-now-completed) state instead of a spurious
     * staleness error. A DIFFERENT key means two distinct, legitimately racing requests — the
     * caller-supplied {@code phase0ConflictException} (unchanged behavior) applies.
     */
    private RuntimeException resolvePhase0Conflict(Assessment assessment, AiOperation operation,
            Function<String, RuntimeException> phase0ConflictException) {
        Optional<AiOperation> winner = aiOperationRepository
                .findLatestByAssessmentIdAndOperationType(assessment.getId(), operation.getOperationType());
        if (winner.isPresent() && winner.get().getIdempotencyKey().equals(operation.getIdempotencyKey())) {
            return new OperationInProgressException(assessment.getId().value().toString());
        }
        return phase0ConflictException.apply(assessment.getId().value().toString());
    }

    private AssessmentRevision runPhase2(Assessment assessment, AiOperation operation, AgentAttempt attempt,
            AssessmentAgentResponse response, Predicate<Assessment> staleAtCompletion,
            BiFunction<AgentAttempt, AssessmentAgentResponse, AssessmentRevision> revisionFactory,
            IdempotencyCompletionContext idempotencyContext) {
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

                // A3 Final Idempotency Correction § 3.1/3.3: the IdempotencyRecord is written
                // inside this same transaction — if this write fails, everything above rolls back
                // together, so a durable success is never left without its replay record.
                recordIdempotencyCompletion(idempotencyContext, revision.getId().toString(), 201);

                revisionRef.set(revision);
            });
        } catch (ObjectOptimisticLockingFailureException ex) {
            persistStaleOnCompletion(operation, attempt, response, structuredResultJson, idempotencyContext);
            throw new StaleOnCompletionException(assessment.getId().value().toString());
        }

        if (staleAtReadTime.get()) {
            persistStaleOnCompletion(operation, attempt, response, structuredResultJson, idempotencyContext);
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
                                           String structuredResultJson, IdempotencyCompletionContext idempotencyContext) {
        transactionTemplate.executeWithoutResult(status -> {
            agentAttemptRepository.save(attempt.markFailed("STALE_ON_COMPLETION",
                    response.log().provider(), response.log().model(), structuredResultJson));
            aiOperationRepository.save(operation.markFailedTerminal());
            recordIdempotencyCompletion(idempotencyContext, operation.getId().toString(),
                    failureResponseStatus(idempotencyContext, "STALE_ON_COMPLETION"));
        });
    }

    private void persistDispatchFailure(AiOperation operation, AgentAttempt attempt, AgentClientException ex,
                                         IdempotencyCompletionContext idempotencyContext) {
        String failureCode = mapFailureCode(ex);
        boolean retryable = isRetryable(failureCode);
        String resolvedProvider = resolvedProviderFrom(ex);
        String resolvedModel = resolvedModelFrom(ex);
        transactionTemplate.executeWithoutResult(status -> {
            agentAttemptRepository.save(attempt.markFailed(failureCode, resolvedProvider, resolvedModel, null));
            AiOperation failed = retryable ? operation.markFailedRetryable() : operation.markFailedTerminal();
            aiOperationRepository.save(failed);
            recordIdempotencyCompletion(idempotencyContext, operation.getId().toString(),
                    failureResponseStatus(idempotencyContext, failureCode));
        });
    }

    /** No-op when {@code context} is {@code null} (retry never carries an idempotency key). */
    private void recordIdempotencyCompletion(IdempotencyCompletionContext context, String resultReference, int responseStatus) {
        if (context == null) {
            return;
        }
        idempotencyGuard.record(context.scope(), context.operationType(), context.idempotencyKey(),
                context.requestPayloadHash(), resultReference, responseStatus);
    }

    /**
     * Initial generation always replays a durable failure as {@code 202} (the same status
     * {@link GenerateAssessmentDraftHandler} returns for a fresh dispatch failure). Regenerate
     * never returns {@code 202} — it derives the original failure's real HTTP status from the
     * failure code via {@link RegenerateFailureReplay}, so a replay can reproduce that same status.
     */
    private static int failureResponseStatus(IdempotencyCompletionContext context, String failureCode) {
        if (context != null && AiOperationType.CREATE_INITIAL_REVISION.name().equals(context.operationType())) {
            return 202;
        }
        return RegenerateFailureReplay.httpStatus(failureCode);
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
