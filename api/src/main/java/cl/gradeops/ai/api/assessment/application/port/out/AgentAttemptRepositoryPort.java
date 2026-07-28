package cl.gradeops.ai.api.assessment.application.port.out;

import cl.gradeops.ai.api.assessment.domain.model.AgentAttempt;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgentAttemptRepositoryPort {
    void save(AgentAttempt attempt);
    Optional<AgentAttempt> findById(UUID id);
    List<AgentAttempt> findAllByAiOperationId(UUID aiOperationId);
}
