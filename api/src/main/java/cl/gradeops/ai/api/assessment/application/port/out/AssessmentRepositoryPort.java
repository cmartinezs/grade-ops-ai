package cl.gradeops.ai.api.assessment.application.port.out;

import cl.gradeops.ai.api.assessment.application.result.AssessmentSummaryResult;
import java.util.List;

// Stub port — Epic 02 will replace return type with domain Assessment objects
public interface AssessmentRepositoryPort {
    List<AssessmentSummaryResult> findAllByTeacherId(String teacherUid);
}
