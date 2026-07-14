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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.UUID;
import java.util.function.BiFunction;

/**
 * NO @Service — declared as @Bean in AssessmentConfig (task-05 pattern).
 *
 * <p>Shared "call agents/ outside a transaction, then persist AgentExecutionLog +
 * AssessmentDraft inside one" logic used by both {@code GenerateAssessmentDraftHandler}
 * (task-07, initial generation) and {@code RegenerateAssessmentDraftHandler} (task-08,
 * regeneration) — extracted here per task-08's design so neither handler duplicates this
 * ordering-sensitive logic (see task-07's Risk callout on why the ordering matters).
 *
 * <p>Uses a programmatic {@link TransactionTemplate} rather than {@code @Transactional} on a
 * sibling method: this class calls {@link AssessmentAgentClient#generate} — a slow external HTTP
 * call — and then persists two rows. Splitting the persist step into a separately
 * {@code @Transactional}-annotated method on {@code this} would silently NOT run inside a
 * transaction (Spring AOP proxies don't intercept self-invocation). {@link TransactionTemplate}
 * sidesteps that: the network call runs with no transaction manager involvement at all, and the
 * transaction is opened only around the persist step, regardless of which object invokes it.
 */
public class DraftGenerationCoordinator {

    private final AssessmentDraftRepositoryPort assessmentDraftRepository;
    private final AgentExecutionLogRepositoryPort agentExecutionLogRepository;
    private final AssessmentAgentClient assessmentAgentClient;
    private final TransactionTemplate transactionTemplate;

    public DraftGenerationCoordinator(AssessmentDraftRepositoryPort assessmentDraftRepository,
                                       AgentExecutionLogRepositoryPort agentExecutionLogRepository,
                                       AssessmentAgentClient assessmentAgentClient,
                                       PlatformTransactionManager transactionManager) {
        this.assessmentDraftRepository = assessmentDraftRepository;
        this.agentExecutionLogRepository = agentExecutionLogRepository;
        this.assessmentAgentClient = assessmentAgentClient;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * @param draftFactory builds the {@link AssessmentDraft} from the agent's result and the
     *                      persisted log's id — {@code AssessmentDraft.generate(...)} for initial
     *                      generation, {@code AssessmentDraft.regenerate(...)} for regeneration.
     *                      Kept as a caller-supplied closure so this class stays agnostic to
     *                      which of the two produced it.
     */
    GenerateAssessmentDraftResult callAgentAndPersist(
            AssessmentId assessmentId, AssessmentCommand agentCommand,
            BiFunction<AssessmentAgentResponse.Result, UUID, AssessmentDraft> draftFactory) {
        // No DB transaction is open here — assessmentAgentClient.generate(...) runs before
        // transactionTemplate.execute(...) is ever called.
        try {
            AssessmentAgentResponse response = assessmentAgentClient.generate(agentCommand);
            return transactionTemplate.execute(status ->
                    persistSuccess(assessmentId, agentCommand, response, draftFactory));
        } catch (AgentClientException ex) {
            transactionTemplate.executeWithoutResult(status -> persistFailure(assessmentId, ex));
            throw ex;
        }
    }

    private GenerateAssessmentDraftResult persistSuccess(
            AssessmentId assessmentId, AssessmentCommand agentCommand, AssessmentAgentResponse response,
            BiFunction<AssessmentAgentResponse.Result, UUID, AssessmentDraft> draftFactory) {
        AssessmentAgentResponse.Log logPayload = response.log();
        AgentExecutionLog log = AgentExecutionLog.create(
                assessmentId, logPayload.agentExecutionId(), logPayload.agentName(), agentCommand.provider(),
                logPayload.model(), logPayload.promptVersion(), logPayload.inputHash(), logPayload.outputHash(),
                logPayload.estimatedInputTokens(), logPayload.estimatedOutputTokens(), logPayload.costEstimate(),
                logPayload.status(), logPayload.errorCode(), logPayload.startedAt(), logPayload.finishedAt());
        agentExecutionLogRepository.save(log);

        AssessmentDraft draft = draftFactory.apply(response.result(), log.getId());
        assessmentDraftRepository.save(draft);

        // Back-fill the log's draftId now that the draft row exists — AssessmentDraftRepositoryPort.save
        // is insert-only (no update path, per task-03), so the cross-reference is completed on the log
        // side instead, which is safe to re-save (upsert by id).
        agentExecutionLogRepository.save(log.withDraftId(draft.getId()));

        return new GenerateAssessmentDraftResult(
                draft.getId(), draft.getTitle(), draft.getContext(), draft.getInstructions(),
                draft.getObjectives(), draft.getDeliverables(), draft.getConstraints(), draft.getVersionNumber());
    }

    private void persistFailure(AssessmentId assessmentId, AgentClientException ex) {
        Instant now = Instant.now();
        AgentExecutionLog log = AgentExecutionLog.create(
                assessmentId, null, "assessment", null, null, null, null, null,
                null, null, null, "FAILED", ex.reason().name(), now, now);
        agentExecutionLogRepository.save(log);
    }
}
