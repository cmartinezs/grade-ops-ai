package cl.gradeops.ai.api.shared.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyRecord;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyScope;
import cl.gradeops.ai.api.shared.application.idempotency.IdempotencyScopeType;

class IdempotencyRecordPersistenceMapper {

    IdempotencyRecord toDomain(IdempotencyRecordJpaEntity e) {
        IdempotencyScopeType scopeType = IdempotencyScopeType.valueOf(e.getScopeType());
        IdempotencyScope scope = scopeType == IdempotencyScopeType.TEACHER
                ? IdempotencyScope.teacher(e.getTeacherUid())
                : IdempotencyScope.assessment(e.getAssessmentId());
        return IdempotencyRecord.restore(e.getId(), scope, e.getOperationType(), e.getIdempotencyKey(),
                e.getRequestPayloadHash(), e.getResultReference(), e.getResponseStatus(),
                e.getCreatedAt(), e.getExpiresAt());
    }

    IdempotencyRecordJpaEntity toEntity(IdempotencyRecord r) {
        IdempotencyRecordJpaEntity e = new IdempotencyRecordJpaEntity();
        e.setId(r.getId());
        e.setScopeType(r.getScope().type().name());
        e.setTeacherUid(r.getScope().teacherUid());
        e.setAssessmentId(r.getScope().assessmentId());
        e.setOperationType(r.getOperationType());
        e.setIdempotencyKey(r.getIdempotencyKey());
        e.setRequestPayloadHash(r.getRequestPayloadHash());
        e.setResultReference(r.getResultReference());
        e.setResponseStatus(r.getResponseStatus());
        e.setCreatedAt(r.getCreatedAt());
        e.setExpiresAt(r.getExpiresAt());
        return e;
    }
}
