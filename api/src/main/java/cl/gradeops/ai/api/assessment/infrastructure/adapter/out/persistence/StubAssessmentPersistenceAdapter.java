package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.AssessmentSummaryResult;
import java.util.List;

// Stub — returns empty list until assessment table is created in Epic 02
// NO @Repository — declared as @Bean in AssessmentConfig
public class StubAssessmentPersistenceAdapter implements AssessmentRepositoryPort {

    @Override
    public List<AssessmentSummaryResult> findAllByTeacherId(String teacherUid) {
        return List.of();
    }
}
