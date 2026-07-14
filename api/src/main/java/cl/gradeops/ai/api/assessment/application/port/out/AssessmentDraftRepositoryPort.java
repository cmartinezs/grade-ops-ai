package cl.gradeops.ai.api.assessment.application.port.out;

import cl.gradeops.ai.api.assessment.domain.model.AssessmentDraft;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;

import java.util.List;
import java.util.Optional;

public interface AssessmentDraftRepositoryPort {
    void save(AssessmentDraft draft);
    Optional<AssessmentDraft> findCurrentByAssessmentId(AssessmentId assessmentId);
    List<AssessmentDraft> findAllByAssessmentId(AssessmentId assessmentId);
}
