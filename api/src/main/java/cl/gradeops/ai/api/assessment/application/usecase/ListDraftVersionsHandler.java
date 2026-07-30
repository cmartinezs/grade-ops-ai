package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.assessment.application.command.ListDraftVersionsCommand;
import cl.gradeops.ai.api.assessment.application.port.in.ListDraftVersionsUseCase;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRevisionRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;

import java.util.List;

/**
 * NO @Service — declared as @Bean in AssessmentConfig (task-05 pattern).
 *
 * <p>Session A3 (Task 10) migrates this handler off the legacy {@code AssessmentDraft} model onto
 * {@code AssessmentRevision} — see {@link GetCurrentDraftHandler}'s javadoc for the same
 * discovered-necessity rationale.
 */
public class ListDraftVersionsHandler implements ListDraftVersionsUseCase {

    private final AssessmentRepositoryPort assessmentRepository;
    private final AssessmentRevisionRepositoryPort assessmentRevisionRepository;
    private final OwnershipVerifier ownershipVerifier;

    public ListDraftVersionsHandler(AssessmentRepositoryPort assessmentRepository,
                                     AssessmentRevisionRepositoryPort assessmentRevisionRepository,
                                     OwnershipVerifier ownershipVerifier) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentRevisionRepository = assessmentRevisionRepository;
        this.ownershipVerifier = ownershipVerifier;
    }

    @Override
    public List<GenerateAssessmentDraftResult> execute(ListDraftVersionsCommand command) {
        AssessmentId assessmentId = new AssessmentId(command.assessmentId());
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));
        ownershipVerifier.verify(assessment.getTeacherUid(), command.teacherUid(), assessmentId.value().toString());

        return assessmentRevisionRepository.findAllByAssessmentId(assessmentId).stream()
                .map(GenerateAssessmentDraftResult::fromRevision)
                .toList();
    }
}
