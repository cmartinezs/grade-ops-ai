package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.domain.model.AiOperation;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationStatus;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationType;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;

public class AiOperationPersistenceMapper {

    AiOperation toDomain(AiOperationJpaEntity e) {
        return AiOperation.restore(
            e.getId(),
            new AssessmentId(e.getAssessmentId()),
            AiOperationType.valueOf(e.getOperationType()),
            e.getRequestedBy(),
            e.getIdempotencyKey(),
            e.getExpectedRevisionId(),
            AiOperationStatus.valueOf(e.getStatus()),
            e.getResultRevisionId(),
            e.getCreatedAt(),
            e.getUpdatedAt()
        );
    }

    AiOperationJpaEntity toEntity(AiOperation op) {
        AiOperationJpaEntity e = new AiOperationJpaEntity();
        e.setId(op.getId());
        e.setAssessmentId(op.getAssessmentId().value());
        e.setOperationType(op.getOperationType().name());
        e.setRequestedBy(op.getRequestedBy());
        e.setIdempotencyKey(op.getIdempotencyKey());
        e.setExpectedRevisionId(op.getExpectedRevisionId());
        e.setStatus(op.getStatus().name());
        e.setResultRevisionId(op.getResultRevisionId());
        e.setCreatedAt(op.getCreatedAt());
        e.setUpdatedAt(op.getUpdatedAt());
        return e;
    }
}
