package cl.gradeops.ai.api.assessment.infrastructure.config;

import cl.gradeops.ai.api.agentclient.AssessmentAgentClient;
import cl.gradeops.ai.api.assessment.application.port.out.AgentExecutionLogRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentBriefRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentDraftRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.usecase.CreateAssessmentBriefHandler;
import cl.gradeops.ai.api.assessment.application.usecase.GenerateAssessmentDraftHandler;
import cl.gradeops.ai.api.assessment.application.usecase.ListAssessmentsHandler;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentExecutionLogJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentExecutionLogPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentExecutionLogPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentDraftJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentDraftPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentDraftPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentPersistenceMapper;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

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
    AssessmentBriefPersistenceMapper assessmentBriefPersistenceMapper() {
        return new AssessmentBriefPersistenceMapper();
    }

    @Bean
    AssessmentBriefPersistenceAdapter assessmentBriefPersistenceAdapter(
            AssessmentBriefJpaRepository jpaRepository,
            AssessmentBriefPersistenceMapper mapper) {
        return new AssessmentBriefPersistenceAdapter(jpaRepository, mapper);
    }

    @Bean
    AssessmentDraftPersistenceMapper assessmentDraftPersistenceMapper() {
        return new AssessmentDraftPersistenceMapper();
    }

    @Bean
    AssessmentDraftPersistenceAdapter assessmentDraftPersistenceAdapter(
            AssessmentDraftJpaRepository jpaRepository,
            AssessmentDraftPersistenceMapper mapper) {
        return new AssessmentDraftPersistenceAdapter(jpaRepository, mapper);
    }

    @Bean
    ListAssessmentsHandler listAssessmentsHandler(AssessmentRepositoryPort assessmentRepository) {
        return new ListAssessmentsHandler(assessmentRepository);
    }

    @Bean
    CreateAssessmentBriefHandler createAssessmentBriefHandler(
            AssessmentRepositoryPort assessmentRepository,
            AssessmentBriefRepositoryPort assessmentBriefRepository) {
        return new CreateAssessmentBriefHandler(assessmentRepository, assessmentBriefRepository);
    }

    @Bean
    AgentExecutionLogPersistenceMapper agentExecutionLogPersistenceMapper() {
        return new AgentExecutionLogPersistenceMapper();
    }

    @Bean
    AgentExecutionLogPersistenceAdapter agentExecutionLogPersistenceAdapter(
            AgentExecutionLogJpaRepository jpaRepository,
            AgentExecutionLogPersistenceMapper mapper) {
        return new AgentExecutionLogPersistenceAdapter(jpaRepository, mapper);
    }

    @Bean
    GenerateAssessmentDraftHandler generateAssessmentDraftHandler(
            AssessmentRepositoryPort assessmentRepository,
            AssessmentBriefRepositoryPort assessmentBriefRepository,
            AssessmentDraftRepositoryPort assessmentDraftRepository,
            AgentExecutionLogRepositoryPort agentExecutionLogRepository,
            AssessmentAgentClient assessmentAgentClient,
            OwnershipVerifier ownershipVerifier,
            PlatformTransactionManager transactionManager) {
        return new GenerateAssessmentDraftHandler(assessmentRepository, assessmentBriefRepository,
                assessmentDraftRepository, agentExecutionLogRepository, assessmentAgentClient,
                ownershipVerifier, transactionManager);
    }
}
