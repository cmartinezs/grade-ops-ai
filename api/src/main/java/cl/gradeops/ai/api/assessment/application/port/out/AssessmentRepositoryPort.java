package cl.gradeops.ai.api.assessment.application.port.out;

import cl.gradeops.ai.api.assessment.application.result.AssessmentSummaryResult;
import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;

import java.util.List;
import java.util.Optional;

public interface AssessmentRepositoryPort {
    void save(Assessment assessment);
    Optional<Assessment> findById(AssessmentId id);
    List<AssessmentSummaryResult> findAllByTeacherId(String teacherUid);
}
