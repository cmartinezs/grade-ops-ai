package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AgentExecutionLogJpaRepository extends JpaRepository<AgentExecutionLogJpaEntity, UUID> {
}
