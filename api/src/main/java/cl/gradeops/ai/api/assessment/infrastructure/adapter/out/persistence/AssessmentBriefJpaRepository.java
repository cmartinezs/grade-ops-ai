package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AssessmentBriefJpaRepository extends JpaRepository<AssessmentBriefJpaEntity, UUID> {
    Optional<AssessmentBriefJpaEntity> findByAssessmentId(UUID assessmentId);
}
