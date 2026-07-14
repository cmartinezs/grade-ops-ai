package cl.gradeops.ai.api.assessment.application.port.out;

import cl.gradeops.ai.api.assessment.domain.model.AgentExecutionLog;

public interface AgentExecutionLogRepositoryPort {
    void save(AgentExecutionLog log);
}
