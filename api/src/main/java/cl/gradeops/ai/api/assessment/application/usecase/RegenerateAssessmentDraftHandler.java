package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AssessmentCommand;
import cl.gradeops.ai.api.assessment.application.command.RegenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.exception.NoPriorDraftException;
import cl.gradeops.ai.api.assessment.application.port.in.RegenerateAssessmentDraftUseCase;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentBriefRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentDraftRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentDraft;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;

/**
 * NO @Service — declared as @Bean in AssessmentConfig (task-05 pattern).
 *
 * <p>Shares the "call agents/, persist log + draft" logic with {@code GenerateAssessmentDraftHandler}
 * via {@link DraftGenerationCoordinator} (task-08) — the only differences are the extra
 * {@code adjustmentNotes}/{@code previousDraftId}/{@code previousDraft} fields on the outgoing
 * command, and that the resulting draft is built via {@code AssessmentDraft.regenerate(...)}
 * instead of {@code generate(...)}.
 */
public class RegenerateAssessmentDraftHandler implements RegenerateAssessmentDraftUseCase {

    private final AssessmentRepositoryPort assessmentRepository;
    private final AssessmentBriefRepositoryPort assessmentBriefRepository;
    private final AssessmentDraftRepositoryPort assessmentDraftRepository;
    private final OwnershipVerifier ownershipVerifier;
    private final DraftGenerationCoordinator draftGenerationCoordinator;

    public RegenerateAssessmentDraftHandler(AssessmentRepositoryPort assessmentRepository,
                                             AssessmentBriefRepositoryPort assessmentBriefRepository,
                                             AssessmentDraftRepositoryPort assessmentDraftRepository,
                                             OwnershipVerifier ownershipVerifier,
                                             DraftGenerationCoordinator draftGenerationCoordinator) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentBriefRepository = assessmentBriefRepository;
        this.assessmentDraftRepository = assessmentDraftRepository;
        this.ownershipVerifier = ownershipVerifier;
        this.draftGenerationCoordinator = draftGenerationCoordinator;
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

        return draftGenerationCoordinator.callAgentAndPersist(assessmentId, agentCommand,
                (result, logId) -> AssessmentDraft.regenerate(currentDraft, result.title(), result.context(),
                        result.instructions(), result.objectives(), result.deliverables(), result.constraints(), logId));
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
