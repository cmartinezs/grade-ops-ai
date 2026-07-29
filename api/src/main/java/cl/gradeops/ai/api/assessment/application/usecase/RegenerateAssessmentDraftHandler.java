package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AgentClientException;
import cl.gradeops.ai.api.agentclient.AssessmentAgentClient;
import cl.gradeops.ai.api.agentclient.AssessmentAgentResponse;
import cl.gradeops.ai.api.agentclient.AssessmentCommand;
import cl.gradeops.ai.api.assessment.application.command.RegenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.exception.NoPriorDraftException;
import cl.gradeops.ai.api.assessment.application.port.in.RegenerateAssessmentDraftUseCase;
import cl.gradeops.ai.api.assessment.application.port.out.AgentExecutionLogRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentBriefRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentDraftRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentDraft;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.assessment.domain.model.AgentExecutionLog;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.UUID;

/**
 * NO @Service — declared as @Bean in AssessmentConfig (task-05 pattern).
 *
 * <p>Still operates on the legacy {@code AssessmentDraft}/{@code AgentExecutionLog} model —
 * unlike {@link GenerateAssessmentDraftHandler}, this handler is not part of Task 07B's scope
 * (see TASKS.md Task 09, a later session, which migrates regenerate onto {@code
 * AssessmentRevision}/{@link AiOperationCoordinator}). The "call agents/ outside a transaction,
 * then persist inside one" logic previously shared with {@code GenerateAssessmentDraftHandler}
 * via the shared coordinator this packet's Task 07B removes is inlined here unchanged, since
 * this is its only remaining caller this session.
 */
public class RegenerateAssessmentDraftHandler implements RegenerateAssessmentDraftUseCase {

    private final AssessmentRepositoryPort assessmentRepository;
    private final AssessmentBriefRepositoryPort assessmentBriefRepository;
    private final AssessmentDraftRepositoryPort assessmentDraftRepository;
    private final AgentExecutionLogRepositoryPort agentExecutionLogRepository;
    private final OwnershipVerifier ownershipVerifier;
    private final AssessmentAgentClient assessmentAgentClient;
    private final TransactionTemplate transactionTemplate;

    public RegenerateAssessmentDraftHandler(AssessmentRepositoryPort assessmentRepository,
                                             AssessmentBriefRepositoryPort assessmentBriefRepository,
                                             AssessmentDraftRepositoryPort assessmentDraftRepository,
                                             AgentExecutionLogRepositoryPort agentExecutionLogRepository,
                                             OwnershipVerifier ownershipVerifier,
                                             AssessmentAgentClient assessmentAgentClient,
                                             PlatformTransactionManager transactionManager) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentBriefRepository = assessmentBriefRepository;
        this.assessmentDraftRepository = assessmentDraftRepository;
        this.agentExecutionLogRepository = agentExecutionLogRepository;
        this.ownershipVerifier = ownershipVerifier;
        this.assessmentAgentClient = assessmentAgentClient;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public GenerateAssessmentDraftResult execute(RegenerateAssessmentDraftCommand command) {
        AssessmentId assessmentId = new AssessmentId(command.assessmentId());
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));
        ownershipVerifier.verify(assessment.getTeacherUid(), command.teacherUid(), assessmentId.value().toString());

        AssessmentBrief brief = assessmentBriefRepository.findByAssessmentId(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));

        AssessmentDraft currentDraft = assessmentDraftRepository.findCurrentByAssessmentId(assessmentId)
                .orElseThrow(() -> new NoPriorDraftException(assessmentId.value().toString()));

        AssessmentCommand agentCommand = new AssessmentCommand(
                brief.getLearningGoal(), brief.getTopic(), brief.getLevel(), brief.getDuration(), brief.getLanguage(),
                command.adjustmentNotes(), currentDraft.getId().toString(), toPromptSummary(currentDraft), null, null);

        return callAgentAndPersist(assessmentId, agentCommand, currentDraft);
    }

    private GenerateAssessmentDraftResult callAgentAndPersist(
            AssessmentId assessmentId, AssessmentCommand agentCommand, AssessmentDraft currentDraft) {
        String correlationId = UUID.randomUUID().toString();
        try {
            AssessmentAgentResponse response = assessmentAgentClient.generate(agentCommand, correlationId);
            return transactionTemplate.execute(status -> persistSuccess(assessmentId, response, currentDraft));
        } catch (AgentClientException ex) {
            transactionTemplate.executeWithoutResult(status -> persistFailure(assessmentId, ex));
            throw ex;
        }
    }

    private GenerateAssessmentDraftResult persistSuccess(
            AssessmentId assessmentId, AssessmentAgentResponse response, AssessmentDraft currentDraft) {
        AssessmentAgentResponse.Log logPayload = response.log();
        // Unchanged from the previous shared coordinator: persists the *requested* provider
        // (often null), not logPayload.provider() (the actually-resolved one) — fixing this for
        // regenerate is Task 09's job (it migrates this handler onto AssessmentRevision
        // entirely), not this inlined-for-compilation legacy path's.
        AgentExecutionLog log = AgentExecutionLog.create(
                assessmentId, logPayload.agentExecutionId(), logPayload.agentName(), null,
                logPayload.model(), logPayload.promptVersion(), logPayload.inputHash(), logPayload.outputHash(),
                logPayload.estimatedInputTokens(), logPayload.estimatedOutputTokens(), logPayload.costEstimate(),
                logPayload.status(), logPayload.errorCode(), logPayload.startedAt(), logPayload.finishedAt());
        agentExecutionLogRepository.save(log);

        AssessmentDraft draft = AssessmentDraft.regenerate(currentDraft, response.result().title(),
                response.result().context(), response.result().instructions(), response.result().objectives(),
                response.result().deliverables(), response.result().constraints(), log.getId());
        assessmentDraftRepository.save(draft);

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

    /** Renders the prior draft's content to text for {@code agents/}'s {@code previousDraft} field. */
    private static String toPromptSummary(AssessmentDraft draft) {
        return "Title: " + draft.getTitle()
                + "\nContext: " + draft.getContext()
                + "\nInstructions: " + draft.getInstructions()
                + "\nObjectives: " + String.join("; ", draft.getObjectives())
                + "\nDeliverables: " + String.join("; ", draft.getDeliverables())
                + "\nConstraints: " + String.join("; ", draft.getConstraints());
    }
}
