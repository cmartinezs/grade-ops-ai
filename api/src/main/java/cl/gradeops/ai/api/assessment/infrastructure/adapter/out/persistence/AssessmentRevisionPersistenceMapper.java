package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision;
import cl.gradeops.ai.api.assessment.domain.model.RevisionOrigin;

public class AssessmentRevisionPersistenceMapper {

    AssessmentRevision toDomain(AssessmentRevisionJpaEntity e) {
        return AssessmentRevision.restore(
            e.getId(),
            new AssessmentId(e.getAssessmentId()),
            e.getVersionNumber(),
            e.getPreviousRevisionId(),
            RevisionOrigin.valueOf(e.getOrigin()),
            e.getActorId(),
            e.getReason(),
            e.getSourceAgentAttemptId(),
            e.getTitle(),
            e.getContext(),
            e.getInstructions(),
            e.getObjectives(),
            e.getDeliverables(),
            e.getConstraints(),
            e.getCreatedAt()
        );
    }

    AssessmentRevisionJpaEntity toEntity(AssessmentRevision r) {
        AssessmentRevisionJpaEntity e = new AssessmentRevisionJpaEntity();
        e.setId(r.getId());
        e.setAssessmentId(r.getAssessmentId().value());
        e.setVersionNumber(r.getVersionNumber());
        e.setPreviousRevisionId(r.getPreviousRevisionId());
        e.setOrigin(r.getOrigin().name());
        e.setActorId(r.getActorId());
        e.setReason(r.getReason());
        e.setSourceAgentAttemptId(r.getSourceAgentAttemptId());
        e.setTitle(r.getTitle());
        e.setContext(r.getContext());
        e.setInstructions(r.getInstructions());
        e.setObjectives(r.getObjectives());
        e.setDeliverables(r.getDeliverables());
        e.setConstraints(r.getConstraints());
        e.setCreatedAt(r.getCreatedAt());
        return e;
    }
}
