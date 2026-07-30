package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AiOperationJpaRepository extends JpaRepository<AiOperationJpaEntity, UUID> {
    Optional<AiOperationJpaEntity> findFirstByAssessmentIdAndOperationTypeOrderByCreatedAtDesc(
            UUID assessmentId, String operationType);
}
