package cl.gradeops.ai.api.shared.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyRecordJpaRepository extends JpaRepository<IdempotencyRecordJpaEntity, UUID> {

    Optional<IdempotencyRecordJpaEntity> findByScopeTypeAndTeacherUidAndOperationTypeAndIdempotencyKey(
            String scopeType, String teacherUid, String operationType, String idempotencyKey);

    Optional<IdempotencyRecordJpaEntity> findByScopeTypeAndAssessmentIdAndOperationTypeAndIdempotencyKey(
            String scopeType, UUID assessmentId, String operationType, String idempotencyKey);
}
