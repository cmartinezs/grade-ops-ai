package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.application.port.out.AiOperationRepositoryPort;
import cl.gradeops.ai.api.assessment.domain.model.AiOperation;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class AiOperationPersistenceAdapter implements AiOperationRepositoryPort {

    private final AiOperationJpaRepository jpaRepository;
    private final AiOperationPersistenceMapper mapper;

    @Override
    public void save(AiOperation operation) {
        jpaRepository.save(mapper.toEntity(operation));
    }

    @Override
    public Optional<AiOperation> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
