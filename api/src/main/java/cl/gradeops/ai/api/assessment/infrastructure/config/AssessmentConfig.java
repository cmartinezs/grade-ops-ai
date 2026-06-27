package cl.gradeops.ai.api.assessment.infrastructure.config;

import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.usecase.ListAssessmentsHandler;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.StubAssessmentPersistenceAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AssessmentConfig {

    @Bean
    StubAssessmentPersistenceAdapter stubAssessmentPersistenceAdapter() {
        return new StubAssessmentPersistenceAdapter();
    }

    @Bean
    ListAssessmentsHandler listAssessmentsHandler(AssessmentRepositoryPort assessmentRepository) {
        return new ListAssessmentsHandler(assessmentRepository);
    }
}
