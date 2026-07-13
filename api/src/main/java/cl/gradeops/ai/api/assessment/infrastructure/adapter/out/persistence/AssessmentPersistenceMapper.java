package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.domain.model.Assessment;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentStatus;

public class AssessmentPersistenceMapper {

    Assessment toDomain(AssessmentJpaEntity e) {
        return Assessment.restore(
            new AssessmentId(e.getId()),
            e.getTeacherUid(),
            AssessmentStatus.valueOf(e.getStatus()),
            e.getCreatedAt()
        );
    }

    AssessmentJpaEntity toEntity(Assessment a) {
        AssessmentJpaEntity e = new AssessmentJpaEntity();
        e.setId(a.getId().value());
        e.setTeacherUid(a.getTeacherUid());
        e.setStatus(a.getStatus().name());
        e.setCreatedAt(a.getCreatedAt());
        return e;
    }
}
