package cl.gradeops.ai.api.assessment.infrastructure.config;

import cl.gradeops.ai.api.agentclient.AssessmentAgentClient;
import cl.gradeops.ai.api.assessment.application.port.out.AgentAttemptRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AiOperationRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentBriefRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentDraftRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRepositoryPort;
import cl.gradeops.ai.api.assessment.application.port.out.AssessmentRevisionRepositoryPort;
import cl.gradeops.ai.api.assessment.application.usecase.AiOperationCoordinator;
import cl.gradeops.ai.api.assessment.application.usecase.CreateAssessmentBriefHandler;
import cl.gradeops.ai.api.assessment.application.usecase.CreateHumanRevisionHandler;
import cl.gradeops.ai.api.assessment.application.usecase.GenerateAssessmentDraftHandler;
import cl.gradeops.ai.api.assessment.application.usecase.GetCurrentDraftHandler;
import cl.gradeops.ai.api.assessment.application.usecase.ListAssessmentsHandler;
import cl.gradeops.ai.api.assessment.application.usecase.ListDraftVersionsHandler;
import cl.gradeops.ai.api.assessment.application.usecase.RegenerateAssessmentDraftHandler;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentAttemptJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentAttemptPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentAttemptPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentExecutionLogJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentExecutionLogPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AgentExecutionLogPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AiOperationJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AiOperationPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AiOperationPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentBriefPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentDraftJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentDraftPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentDraftPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentPersistenceMapper;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentRevisionJpaRepository;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentRevisionPersistenceAdapter;
import cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence.AssessmentRevisionPersistenceMapper;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyGuard;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyRepositoryPort;
import cl.gradeops.ai.api.shared.application.security.OwnershipVerifier;
import cl.gradeops.ai.api.shared.infrastructure.adapter.out.persistence.IdempotencyRecordJpaRepository;
import cl.gradeops.ai.api.shared.infrastructure.adapter.out.persistence.IdempotencyRecordPersistenceAdapter;
import cl.gradeops.ai.api.shared.infrastructure.adapter.out.persistence.IdempotencyRecordPersistenceMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import tools.jackson.databind.json.JsonMapper;

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
    AssessmentRevisionPersistenceMapper assessmentRevisionPersistenceMapper() {
        return new AssessmentRevisionPersistenceMapper();
    }

    @Bean
    AssessmentRevisionPersistenceAdapter assessmentRevisionPersistenceAdapter(
            AssessmentRevisionJpaRepository jpaRepository,
            AssessmentRevisionPersistenceMapper mapper) {
        return new AssessmentRevisionPersistenceAdapter(jpaRepository, mapper);
    }

    @Bean
    AiOperationPersistenceMapper aiOperationPersistenceMapper() {
        return new AiOperationPersistenceMapper();
    }

    @Bean
    AiOperationPersistenceAdapter aiOperationPersistenceAdapter(
            AiOperationJpaRepository jpaRepository,
            AiOperationPersistenceMapper mapper) {
        return new AiOperationPersistenceAdapter(jpaRepository, mapper);
    }

    @Bean
    AgentAttemptPersistenceMapper agentAttemptPersistenceMapper() {
        return new AgentAttemptPersistenceMapper();
    }

    @Bean
    AgentAttemptPersistenceAdapter agentAttemptPersistenceAdapter(
            AgentAttemptJpaRepository jpaRepository,
            AgentAttemptPersistenceMapper mapper) {
        return new AgentAttemptPersistenceAdapter(jpaRepository, mapper);
    }

    @Bean
    IdempotencyRecordPersistenceMapper idempotencyRecordPersistenceMapper() {
        return new IdempotencyRecordPersistenceMapper();
    }

    @Bean
    IdempotencyRecordPersistenceAdapter idempotencyRecordPersistenceAdapter(
            IdempotencyRecordJpaRepository jpaRepository,
            IdempotencyRecordPersistenceMapper mapper) {
        return new IdempotencyRecordPersistenceAdapter(jpaRepository, mapper);
    }

    @Bean
    IdempotencyGuard idempotencyGuard(IdempotencyRepositoryPort idempotencyRepository) {
        return new IdempotencyGuard(idempotencyRepository);
    }

    @Bean
    AiOperationCoordinator aiOperationCoordinator(
            AssessmentRepositoryPort assessmentRepository,
            AiOperationRepositoryPort aiOperationRepository,
            AgentAttemptRepositoryPort agentAttemptRepository,
            AssessmentRevisionRepositoryPort assessmentRevisionRepository,
            AssessmentAgentClient assessmentAgentClient,
            JsonMapper jsonMapper,
            PlatformTransactionManager transactionManager) {
        return new AiOperationCoordinator(assessmentRepository, aiOperationRepository, agentAttemptRepository,
                assessmentRevisionRepository, assessmentAgentClient, jsonMapper, transactionManager);
    }

    @Bean
    GenerateAssessmentDraftHandler generateAssessmentDraftHandler(
            AssessmentRepositoryPort assessmentRepository,
            AssessmentBriefRepositoryPort assessmentBriefRepository,
            AssessmentRevisionRepositoryPort assessmentRevisionRepository,
            OwnershipVerifier ownershipVerifier,
            IdempotencyGuard idempotencyGuard,
            AiOperationCoordinator aiOperationCoordinator) {
        return new GenerateAssessmentDraftHandler(assessmentRepository, assessmentBriefRepository,
                assessmentRevisionRepository, ownershipVerifier, idempotencyGuard, aiOperationCoordinator);
    }

    @Bean
    RegenerateAssessmentDraftHandler regenerateAssessmentDraftHandler(
            AssessmentRepositoryPort assessmentRepository,
            AssessmentBriefRepositoryPort assessmentBriefRepository,
            AssessmentRevisionRepositoryPort assessmentRevisionRepository,
            OwnershipVerifier ownershipVerifier,
            IdempotencyGuard idempotencyGuard,
            AiOperationCoordinator aiOperationCoordinator) {
        return new RegenerateAssessmentDraftHandler(assessmentRepository, assessmentBriefRepository,
                assessmentRevisionRepository, ownershipVerifier, idempotencyGuard, aiOperationCoordinator);
    }

    @Bean
    CreateHumanRevisionHandler createHumanRevisionHandler(
            AssessmentRepositoryPort assessmentRepository,
            AssessmentRevisionRepositoryPort assessmentRevisionRepository,
            OwnershipVerifier ownershipVerifier,
            PlatformTransactionManager transactionManager) {
        return new CreateHumanRevisionHandler(assessmentRepository, assessmentRevisionRepository,
                ownershipVerifier, transactionManager);
    }

    @Bean
    GetCurrentDraftHandler getCurrentDraftHandler(
            AssessmentRepositoryPort assessmentRepository,
            AssessmentDraftRepositoryPort assessmentDraftRepository,
            OwnershipVerifier ownershipVerifier) {
        return new GetCurrentDraftHandler(assessmentRepository, assessmentDraftRepository, ownershipVerifier);
    }

    @Bean
    ListDraftVersionsHandler listDraftVersionsHandler(
            AssessmentRepositoryPort assessmentRepository,
            AssessmentDraftRepositoryPort assessmentDraftRepository,
            OwnershipVerifier ownershipVerifier) {
        return new ListDraftVersionsHandler(assessmentRepository, assessmentDraftRepository, ownershipVerifier);
    }
}
