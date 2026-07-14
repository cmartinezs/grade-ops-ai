package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.assessment.application.command.UpdateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.exception.NoPriorDraftException;
import cl.gradeops.ai.api.assessment.application.port.in.UpdateAssessmentDraftUseCase;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentDraftRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentDraft;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;

/**
 * NO @Service — declared as @Bean in AssessmentConfig (task-05 pattern).
 *
 * <p>Unlike {@code GenerateAssessmentDraftHandler}/{@code RegenerateAssessmentDraftHandler}, this
 * handler makes no call to {@code agents/} and does not use {@link DraftGenerationCoordinator} —
 * it updates the current draft's row in place (US-013), so no {@code AgentExecutionLog} is
 * produced and no new version is created. {@code assessmentDraftRepository.save(...)} is called
 * with a domain object that reuses the current draft's {@code id}, so the persistence adapter's
 * underlying JPA {@code save()} performs an UPDATE (upsert-by-id), not an INSERT — the same
 * mechanism task-07 already relies on to back-fill {@code AgentExecutionLog.draftId}.
 */
public class UpdateAssessmentDraftHandler implements UpdateAssessmentDraftUseCase {

    private final AssessmentRepositoryPort assessmentRepository;
    private final AssessmentDraftRepositoryPort assessmentDraftRepository;
    private final OwnershipVerifier ownershipVerifier;

    public UpdateAssessmentDraftHandler(AssessmentRepositoryPort assessmentRepository,
                                         AssessmentDraftRepositoryPort assessmentDraftRepository,
                                         OwnershipVerifier ownershipVerifier) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentDraftRepository = assessmentDraftRepository;
        this.ownershipVerifier = ownershipVerifier;
    }

    @Override
    public GenerateAssessmentDraftResult execute(UpdateAssessmentDraftCommand command) {
        AssessmentId assessmentId = new AssessmentId(command.assessmentId());
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));
        ownershipVerifier.verify(assessment.getTeacherUid(), command.teacherUid(), assessmentId.value().toString());

        AssessmentDraft currentDraft = assessmentDraftRepository.findCurrentByAssessmentId(assessmentId)
                .orElseThrow(() -> new NoPriorDraftException(assessmentId.value().toString()));

        AssessmentDraft updated = currentDraft.applyEdit(command.title(), command.context(), command.instructions(),
                command.objectives(), command.deliverables(), command.constraints());
        assessmentDraftRepository.save(updated);

        return new GenerateAssessmentDraftResult(updated.getId(), updated.getTitle(), updated.getContext(),
                updated.getInstructions(), updated.getObjectives(), updated.getDeliverables(),
                updated.getConstraints(), updated.getVersionNumber());
    }
}
