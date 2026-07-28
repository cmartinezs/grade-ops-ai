package cl.gradeops.ai.api.assessment.application.port.out;

import cl.gradeops.ai.api.assessment.domain.model.AiOperation;

import java.util.Optional;
import java.util.UUID;

public interface AiOperationRepositoryPort {
    void save(AiOperation operation);
    Optional<AiOperation> findById(UUID id);
}
