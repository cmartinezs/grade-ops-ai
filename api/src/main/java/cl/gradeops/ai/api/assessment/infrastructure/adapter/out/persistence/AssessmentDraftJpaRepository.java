package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssessmentDraftJpaRepository extends JpaRepository<AssessmentDraftJpaEntity, UUID> {
    List<AssessmentDraftJpaEntity> findAllByAssessmentIdOrderByVersionNumberDesc(UUID assessmentId);
}
