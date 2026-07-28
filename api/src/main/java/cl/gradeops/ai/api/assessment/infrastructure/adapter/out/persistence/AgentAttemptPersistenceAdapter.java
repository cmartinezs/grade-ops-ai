package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.application.port.out.AgentAttemptRepositoryPort;
import cl.gradeops.ai.api.assessment.domain.model.AgentAttempt;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class AgentAttemptPersistenceAdapter implements AgentAttemptRepositoryPort {

    private final AgentAttemptJpaRepository jpaRepository;
    private final AgentAttemptPersistenceMapper mapper;

    @Override
    public void save(AgentAttempt attempt) {
        jpaRepository.save(mapper.toEntity(attempt));
    }

    @Override
    public Optional<AgentAttempt> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<AgentAttempt> findAllByAiOperationId(UUID aiOperationId) {
        return jpaRepository.findAllByAiOperationIdOrderByAttemptNumberDesc(aiOperationId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
