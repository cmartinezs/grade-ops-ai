package cl.gradeops.ai.api.assessment.infrastructure.config;

import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.usecase.ListAssessmentsHandler;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AssessmentConfig {

    @Bean
    AssessmentPersistenceMapper assessmentPersistenceMapper() {
        return new AssessmentPersistenceMapper();
    }

    @Bean
    AssessmentPersistenceAdapter assessmentPersistenceAdapter(
            AssessmentJpaRepository jpaRepository,
            AssessmentPersistenceMapper mapper) {
        return new AssessmentPersistenceAdapter(jpaRepository, mapper);
    }

    @Bean
    ListAssessmentsHandler listAssessmentsHandler(AssessmentRepositoryPort assessmentRepository) {
        return new ListAssessmentsHandler(assessmentRepository);
    }
}
