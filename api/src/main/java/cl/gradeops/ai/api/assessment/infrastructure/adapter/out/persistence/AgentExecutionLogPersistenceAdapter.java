package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.application.port.out.AgentExecutionLogRepositoryPort;
import cl.gradeops.ai.api.assessment.domain.model.AgentExecutionLog;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AgentExecutionLogPersistenceAdapter implements AgentExecutionLogRepositoryPort {

    private final AgentExecutionLogJpaRepository jpaRepository;
    private final AgentExecutionLogPersistenceMapper mapper;

    @Override
    public void save(AgentExecutionLog log) {
        jpaRepository.save(mapper.toEntity(log));
    }
}
