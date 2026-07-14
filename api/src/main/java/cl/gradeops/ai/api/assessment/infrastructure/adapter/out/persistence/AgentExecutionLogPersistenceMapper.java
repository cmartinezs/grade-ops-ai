package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.domain.model.AgentExecutionLog;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;

public class AgentExecutionLogPersistenceMapper {

    AgentExecutionLog toDomain(AgentExecutionLogJpaEntity e) {
        return AgentExecutionLog.restore(
            e.getId(),
            new AssessmentId(e.getAssessmentId()),
            e.getDraftId(),
            e.getAgentExecutionId(),
            e.getAgentName(),
            e.getProvider(),
            e.getModel(),
            e.getPromptVersion(),
            e.getInputHash(),
            e.getOutputHash(),
            e.getEstimatedInputTokens(),
            e.getEstimatedOutputTokens(),
            e.getCostEstimate(),
            e.getStatus(),
            e.getErrorCode(),
            e.getStartedAt(),
            e.getFinishedAt()
        );
    }

    AgentExecutionLogJpaEntity toEntity(AgentExecutionLog log) {
        AgentExecutionLogJpaEntity e = new AgentExecutionLogJpaEntity();
        e.setId(log.getId());
        e.setAssessmentId(log.getAssessmentId().value());
        e.setDraftId(log.getDraftId());
        e.setAgentExecutionId(log.getAgentExecutionId());
        e.setAgentName(log.getAgentName());
        e.setProvider(log.getProvider());
        e.setModel(log.getModel());
        e.setPromptVersion(log.getPromptVersion());
        e.setInputHash(log.getInputHash());
        e.setOutputHash(log.getOutputHash());
        e.setEstimatedInputTokens(log.getEstimatedInputTokens());
        e.setEstimatedOutputTokens(log.getEstimatedOutputTokens());
        e.setCostEstimate(log.getCostEstimate());
        e.setStatus(log.getStatus());
        e.setErrorCode(log.getErrorCode());
        e.setStartedAt(log.getStartedAt());
        e.setFinishedAt(log.getFinishedAt());
        return e;
    }
}
