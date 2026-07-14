package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.assessment.application.command.GetCurrentDraftCommand;
import cl.gradeops.ai.api.assessment.application.port.in.GetCurrentDraftUseCase;
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
 * <p>Unlike task-08/09's {@code NoPriorDraftException} (422 — a write-operation precondition),
 * "no draft yet" here is a plain 404: {@code GET .../draft} simply has no resource to return.
 */
public class GetCurrentDraftHandler implements GetCurrentDraftUseCase {

    private final AssessmentRepositoryPort assessmentRepository;
    private final AssessmentDraftRepositoryPort assessmentDraftRepository;
    private final OwnershipVerifier ownershipVerifier;

    public GetCurrentDraftHandler(AssessmentRepositoryPort assessmentRepository,
                                   AssessmentDraftRepositoryPort assessmentDraftRepository,
                                   OwnershipVerifier ownershipVerifier) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentDraftRepository = assessmentDraftRepository;
        this.ownershipVerifier = ownershipVerifier;
    }

    @Override
    public GenerateAssessmentDraftResult execute(GetCurrentDraftCommand command) {
        AssessmentId assessmentId = new AssessmentId(command.assessmentId());
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));
        ownershipVerifier.verify(assessment.getTeacherUid(), command.teacherUid(), assessmentId.value().toString());

        AssessmentDraft draft = assessmentDraftRepository.findCurrentByAssessmentId(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));

        return new GenerateAssessmentDraftResult(draft.getId(), draft.getTitle(), draft.getContext(),
                draft.getInstructions(), draft.getObjectives(), draft.getDeliverables(), draft.getConstraints(),
                draft.getVersionNumber());
    }
}
