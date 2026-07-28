package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.domain.model.AgentAttempt;
import cl.gradeops.ai.api.assessment.domain.model.AgentAttemptStatus;

public class AgentAttemptPersistenceMapper {

    AgentAttempt toDomain(AgentAttemptJpaEntity e) {
        return AgentAttempt.restore(
            e.getId(),
            e.getAiOperationId(),
            e.getAttemptNumber(),
            e.getAgentName(),
            e.getResolvedProvider(),
            e.getResolvedModel(),
            e.getPromptVersion(),
            e.getCorrelationId(),
            e.getDispatchedAt(),
            e.getCompletedAt(),
            AgentAttemptStatus.valueOf(e.getStatus()),
            e.getProviderRequestId(),
            e.getFailureCode(),
            e.getEstimatedInputTokens(),
            e.getEstimatedOutputTokens(),
            e.getCostEstimate(),
            e.getStructuredResult()
        );
    }

    AgentAttemptJpaEntity toEntity(AgentAttempt a) {
        AgentAttemptJpaEntity e = new AgentAttemptJpaEntity();
        e.setId(a.getId());
        e.setAiOperationId(a.getAiOperationId());
        e.setAttemptNumber(a.getAttemptNumber());
        e.setAgentName(a.getAgentName());
        e.setResolvedProvider(a.getResolvedProvider());
        e.setResolvedModel(a.getResolvedModel());
        e.setPromptVersion(a.getPromptVersion());
        e.setCorrelationId(a.getCorrelationId());
        e.setDispatchedAt(a.getDispatchedAt());
        e.setCompletedAt(a.getCompletedAt());
        e.setStatus(a.getStatus().name());
        e.setProviderRequestId(a.getProviderRequestId());
        e.setFailureCode(a.getFailureCode());
        e.setEstimatedInputTokens(a.getEstimatedInputTokens());
        e.setEstimatedOutputTokens(a.getEstimatedOutputTokens());
        e.setCostEstimate(a.getCostEstimate());
        e.setStructuredResult(a.getStructuredResult());
        return e;
    }
}
