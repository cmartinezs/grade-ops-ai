package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AssessmentCommand;
import cl.gradeops.ai.api.assessment.application.command.GenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.port.in.GenerateAssessmentDraftUseCase;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentBriefRepositoryPort;
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
 * <p>Owns loading the brief and enforcing ownership; the actual "call agents/, persist log +
 * draft" logic is shared with {@code RegenerateAssessmentDraftHandler} via
 * {@link DraftGenerationCoordinator} (task-08).
 */
public class GenerateAssessmentDraftHandler implements GenerateAssessmentDraftUseCase {

    private final AssessmentRepositoryPort assessmentRepository;
    private final AssessmentBriefRepositoryPort assessmentBriefRepository;
    private final OwnershipVerifier ownershipVerifier;
    private final DraftGenerationCoordinator draftGenerationCoordinator;

    public GenerateAssessmentDraftHandler(AssessmentRepositoryPort assessmentRepository,
                                           AssessmentBriefRepositoryPort assessmentBriefRepository,
                                           OwnershipVerifier ownershipVerifier,
                                           DraftGenerationCoordinator draftGenerationCoordinator) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentBriefRepository = assessmentBriefRepository;
        this.ownershipVerifier = ownershipVerifier;
        this.draftGenerationCoordinator = draftGenerationCoordinator;
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

        return draftGenerationCoordinator.callAgentAndPersist(assessmentId, agentCommand,
                (result, logId) -> AssessmentDraft.generate(assessmentId, result.title(), result.context(),
                        result.instructions(), result.objectives(), result.deliverables(), result.constraints(), logId));
    }
}
