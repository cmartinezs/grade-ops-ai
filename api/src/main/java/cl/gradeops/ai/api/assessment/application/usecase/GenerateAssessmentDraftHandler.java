package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AgentClientException;
import cl.gradeops.ai.api.agentclient.AssessmentAgentClient;
import cl.gradeops.ai.api.agentclient.AssessmentAgentResponse;
import cl.gradeops.ai.api.agentclient.AssessmentCommand;
import cl.gradeops.ai.api.assessment.application.command.GenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.port.in.GenerateAssessmentDraftUseCase;
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
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

/**
 * NO @Service — declared as @Bean in AssessmentConfig (task-05 pattern).
 *
 * <p>Uses a programmatic {@link TransactionTemplate} rather than {@code @Transactional} on a
 * sibling method: this class calls {@link AssessmentAgentClient#generate} — a slow external HTTP
 * call — and then persists two rows. Splitting the persist step into a separately
 * {@code @Transactional}-annotated method on {@code this} would silently NOT run inside a
 * transaction (Spring AOP proxies don't intercept self-invocation), which is exactly the bug this
 * task's design explicitly warns about. {@link TransactionTemplate} sidesteps that: the network
 * call runs with no transaction manager involvement at all, and the transaction is opened only
 * around the persist step, regardless of which object invokes it.
 */
public class GenerateAssessmentDraftHandler implements GenerateAssessmentDraftUseCase {

    private final AssessmentRepositoryPort assessmentRepository;
    private final AssessmentBriefRepositoryPort assessmentBriefRepository;
    private final AssessmentDraftRepositoryPort assessmentDraftRepository;
    private final AgentExecutionLogRepositoryPort agentExecutionLogRepository;
    private final AssessmentAgentClient assessmentAgentClient;
    private final OwnershipVerifier ownershipVerifier;
    private final TransactionTemplate transactionTemplate;

    public GenerateAssessmentDraftHandler(AssessmentRepositoryPort assessmentRepository,
                                           AssessmentBriefRepositoryPort assessmentBriefRepository,
                                           AssessmentDraftRepositoryPort assessmentDraftRepository,
                                           AgentExecutionLogRepositoryPort agentExecutionLogRepository,
                                           AssessmentAgentClient assessmentAgentClient,
                                           OwnershipVerifier ownershipVerifier,
                                           PlatformTransactionManager transactionManager) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentBriefRepository = assessmentBriefRepository;
        this.assessmentDraftRepository = assessmentDraftRepository;
        this.agentExecutionLogRepository = agentExecutionLogRepository;
        this.assessmentAgentClient = assessmentAgentClient;
        this.ownershipVerifier = ownershipVerifier;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public GenerateAssessmentDraftResult execute(GenerateAssessmentDraftCommand command) {
        AssessmentId assessmentId = new AssessmentId(command.assessmentId());
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));
        ownershipVerifier.verify(assessment.getTeacherUid(), command.teacherUid(), assessmentId.value().toString());

        AssessmentBrief brief = assessmentBriefRepository.findByAssessmentId(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));

        AssessmentCommand agentCommand = new AssessmentCommand(
                brief.getLearningGoal(), brief.getTopic(), brief.getLevel(), brief.getDuration(), brief.getLanguage(),
                null, null, null, null, null);

        // No DB transaction is open here — assessmentAgentClient.generate(...) runs before
        // transactionTemplate.execute(...) is ever called.
        try {
            AssessmentAgentResponse response = assessmentAgentClient.generate(agentCommand);
            return transactionTemplate.execute(status -> persistSuccess(assessmentId, agentCommand, response));
        } catch (AgentClientException ex) {
            transactionTemplate.executeWithoutResult(status -> persistFailure(assessmentId, ex));
            throw ex;
        }
    }

    private GenerateAssessmentDraftResult persistSuccess(AssessmentId assessmentId, AssessmentCommand agentCommand,
                                                           AssessmentAgentResponse response) {
        AssessmentAgentResponse.Log logPayload = response.log();
        AgentExecutionLog log = AgentExecutionLog.create(
                assessmentId, logPayload.agentExecutionId(), logPayload.agentName(), agentCommand.provider(),
                logPayload.model(), logPayload.promptVersion(), logPayload.inputHash(), logPayload.outputHash(),
                logPayload.estimatedInputTokens(), logPayload.estimatedOutputTokens(), logPayload.costEstimate(),
                logPayload.status(), logPayload.errorCode(), logPayload.startedAt(), logPayload.finishedAt());
        agentExecutionLogRepository.save(log);

        AssessmentAgentResponse.Result result = response.result();
        AssessmentDraft draft = AssessmentDraft.generate(
                assessmentId, result.title(), result.context(), result.instructions(),
                result.objectives(), result.deliverables(), result.constraints(), log.getId());
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
