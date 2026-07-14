package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.assessment.application.command.ListDraftVersionsCommand;
import cl.gradeops.ai.api.assessment.application.port.in.ListDraftVersionsUseCase;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentDraftRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;

import java.util.List;

/** NO @Service — declared as @Bean in AssessmentConfig (task-05 pattern). */
public class ListDraftVersionsHandler implements ListDraftVersionsUseCase {

    private final AssessmentRepositoryPort assessmentRepository;
    private final AssessmentDraftRepositoryPort assessmentDraftRepository;
    private final OwnershipVerifier ownershipVerifier;

    public ListDraftVersionsHandler(AssessmentRepositoryPort assessmentRepository,
                                     AssessmentDraftRepositoryPort assessmentDraftRepository,
                                     OwnershipVerifier ownershipVerifier) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentDraftRepository = assessmentDraftRepository;
        this.ownershipVerifier = ownershipVerifier;
    }

    @Override
    public List<GenerateAssessmentDraftResult> execute(ListDraftVersionsCommand command) {
        AssessmentId assessmentId = new AssessmentId(command.assessmentId());
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));
        ownershipVerifier.verify(assessment.getTeacherUid(), command.teacherUid(), assessmentId.value().toString());

        return assessmentDraftRepository.findAllByAssessmentId(assessmentId).stream()
                .map(d -> new GenerateAssessmentDraftResult(d.getId(), d.getTitle(), d.getContext(),
                        d.getInstructions(), d.getObjectives(), d.getDeliverables(), d.getConstraints(),
                        d.getVersionNumber()))
                .toList();
    }
}
