package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssessmentRevisionJpaRepository extends JpaRepository<AssessmentRevisionJpaEntity, UUID> {
    List<AssessmentRevisionJpaEntity> findAllByAssessmentIdOrderByVersionNumberDesc(UUID assessmentId);
}
