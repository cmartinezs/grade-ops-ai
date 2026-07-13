package cl.gradeops.ai.api.assessment.application.port.out;

import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;

import java.util.Optional;

public interface AssessmentBriefRepositoryPort {
    void save(AssessmentBrief brief);
    Optional<AssessmentBrief> findByAssessmentId(AssessmentId assessmentId);
}
