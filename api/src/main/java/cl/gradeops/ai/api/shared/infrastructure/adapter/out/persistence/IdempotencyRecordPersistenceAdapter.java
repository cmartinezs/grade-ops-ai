package cl.gradeops.ai.api.shared.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyRecord;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyRepositoryPort;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyScope;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class IdempotencyRecordPersistenceAdapter implements IdempotencyRepositoryPort {

    private final IdempotencyRecordJpaRepository jpaRepository;
    private final IdempotencyRecordPersistenceMapper mapper;

    @Override
    public Optional<IdempotencyRecord> find(IdempotencyScope scope, String operationType, String idempotencyKey) {
        Optional<IdempotencyRecordJpaEntity> entity = switch (scope.type()) {
            case TEACHER -> jpaRepository.findByScopeTypeAndTeacherUidAndOperationTypeAndIdempotencyKey(
                    "TEACHER", scope.teacherUid(), operationType, idempotencyKey);
            case ASSESSMENT -> jpaRepository.findByScopeTypeAndAssessmentIdAndOperationTypeAndIdempotencyKey(
                    "ASSESSMENT", scope.assessmentId(), operationType, idempotencyKey);
        };
        return entity.map(mapper::toDomain);
    }

    @Override
    public void save(IdempotencyRecord record) {
        jpaRepository.save(mapper.toEntity(record));
    }
}
