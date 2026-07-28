package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AgentAttemptJpaRepository extends JpaRepository<AgentAttemptJpaEntity, UUID> {
    List<AgentAttemptJpaEntity> findAllByAiOperationIdOrderByAttemptNumberDesc(UUID aiOperationId);
}
