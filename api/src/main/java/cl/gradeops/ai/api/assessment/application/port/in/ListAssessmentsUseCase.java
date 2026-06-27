package cl.gradeops.ai.api.assessment.application.port.in;

import cl.gradeops.ai.api.assessment.application.result.AssessmentSummaryResult;
import java.util.List;

public interface ListAssessmentsUseCase {
    List<AssessmentSummaryResult> execute(String teacherUid);
}
