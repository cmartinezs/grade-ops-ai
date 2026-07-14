package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import cl.gradeops.ai.api.assessment.domain.model.AssessmentBrief;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;

public class AssessmentBriefPersistenceMapper {

    AssessmentBrief toDomain(AssessmentBriefJpaEntity e) {
        return AssessmentBrief.restore(
            e.getId(),
            new AssessmentId(e.getAssessmentId()),
            e.getLearningGoal(),
            e.getTopic(),
            e.getLevel(),
            e.getDuration(),
            e.getLanguage(),
            e.getCreatedAt()
        );
    }

    AssessmentBriefJpaEntity toEntity(AssessmentBrief b) {
        AssessmentBriefJpaEntity e = new AssessmentBriefJpaEntity();
        e.setId(b.getId());
        e.setAssessmentId(b.getAssessmentId().value());
        e.setLearningGoal(b.getLearningGoal());
        e.setTopic(b.getTopic());
        e.setLevel(b.getLevel());
        e.setDuration(b.getDuration());
        e.setLanguage(b.getLanguage());
        e.setCreatedAt(b.getCreatedAt());
        return e;
    }
}
