package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.domain.model.AssessmentDraft;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;

public class AssessmentDraftPersistenceMapper {

    AssessmentDraft toDomain(AssessmentDraftJpaEntity e) {
        return AssessmentDraft.restore(
            e.getId(),
            new AssessmentId(e.getAssessmentId()),
            e.getVersionNumber(),
            e.getPreviousVersionId(),
            e.getTitle(),
            e.getContext(),
            e.getInstructions(),
            e.getObjectives(),
            e.getDeliverables(),
            e.getConstraints(),
            e.getAgentExecutionLogId(),
            e.getCreatedAt()
        );
    }

    AssessmentDraftJpaEntity toEntity(AssessmentDraft d) {
        AssessmentDraftJpaEntity e = new AssessmentDraftJpaEntity();
        e.setId(d.getId());
        e.setAssessmentId(d.getAssessmentId().value());
        e.setVersionNumber(d.getVersionNumber());
        e.setPreviousVersionId(d.getPreviousVersionId());
        e.setTitle(d.getTitle());
        e.setContext(d.getContext());
        e.setInstructions(d.getInstructions());
        e.setObjectives(d.getObjectives());
        e.setDeliverables(d.getDeliverables());
        e.setConstraints(d.getConstraints());
        e.setAgentExecutionLogId(d.getAgentExecutionLogId());
        e.setCreatedAt(d.getCreatedAt());
        return e;
    }
}
