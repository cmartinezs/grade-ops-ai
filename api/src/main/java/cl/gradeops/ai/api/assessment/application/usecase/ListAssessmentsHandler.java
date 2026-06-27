package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.assessment.application.port.in.ListAssessmentsUseCase;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.result.AssessmentSummaryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

// NO @Service — declared as @Bean in AssessmentConfig (task-05)
@RequiredArgsConstructor
public class ListAssessmentsHandler implements ListAssessmentsUseCase {

    private final AssessmentRepositoryPort assessmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AssessmentSummaryResult> execute(String teacherUid) {
        return assessmentRepository.findAllByTeacherId(teacherUid);
    }
}
