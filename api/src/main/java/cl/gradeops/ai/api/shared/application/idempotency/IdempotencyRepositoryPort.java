package cl.gradeops.ai.api.shared.application.idempotency;

import java.util.Optional;

public interface IdempotencyRepositoryPort {
    Optional<IdempotencyRecord> find(IdempotencyScope scope, String operationType, String idempotencyKey);
    void save(IdempotencyRecord record);
}
