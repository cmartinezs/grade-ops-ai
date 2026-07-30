package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.assessment.application.command.GetCurrentDraftCommand;
import cl.gradeops.ai.api.assessment.application.port.in.GetCurrentDraftUseCase;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRevisionRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
import cl.gradeops.ai.api.shared.domain.exception.ResourceNotFoundException;

/**
 * NO @Service — declared as @Bean in AssessmentConfig (task-05 pattern).
 *
 * <p>Session A3 (Task 10) migrates this handler off the legacy {@code AssessmentDraft} model onto
 * {@code AssessmentRevision}, reading through {@code Assessment.currentRevisionId} — the single
 * authoritative pointer, never {@code MAX(version_number)}-style re-derivation. A discovered
 * necessity, not originally itemized in TASKS.md's Task 10 file list: once Tasks 08/09 moved the
 * write path onto revisions, this handler's old {@code AssessmentDraft}-based read would silently
 * 404 every assessment produced by the new flow — see API-A3-HANDOFF.md.
 *
 * <p>"No draft yet" here is a plain 404 — {@code GET .../draft} simply has no resource to return
 * — distinct from a write-operation precondition failure.
 */
public class GetCurrentDraftHandler implements GetCurrentDraftUseCase {

    private final AssessmentRepositoryPort assessmentRepository;
    private final AssessmentRevisionRepositoryPort assessmentRevisionRepository;
    private final OwnershipVerifier ownershipVerifier;

    public GetCurrentDraftHandler(AssessmentRepositoryPort assessmentRepository,
                                   AssessmentRevisionRepositoryPort assessmentRevisionRepository,
                                   OwnershipVerifier ownershipVerifier) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentRevisionRepository = assessmentRevisionRepository;
        this.ownershipVerifier = ownershipVerifier;
    }

    @Override
    public GenerateAssessmentDraftResult execute(GetCurrentDraftCommand command) {
        AssessmentId assessmentId = new AssessmentId(command.assessmentId());
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));
        ownershipVerifier.verify(assessment.getTeacherUid(), command.teacherUid(), assessmentId.value().toString());

        if (assessment.getCurrentRevisionId() == null) {
            throw new ResourceNotFoundException(assessmentId.value().toString());
        }

        AssessmentRevision revision = assessmentRevisionRepository.findById(assessment.getCurrentRevisionId())
                .orElseThrow(() -> new ResourceNotFoundException(assessmentId.value().toString()));

        return GenerateAssessmentDraftResult.fromRevision(revision);
    }
}
