package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agent_execution_logs")
@Getter
@Setter
@NoArgsConstructor
public class AgentExecutionLogJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "assessment_id", nullable = false)
    private UUID assessmentId;

    @Column(name = "draft_id")
    private UUID draftId;

    @Column(name = "agent_execution_id")
    private UUID agentExecutionId;

    @Column(name = "agent_name")
    private String agentName;

    @Column(name = "provider")
    private String provider;

    @Column(name = "model")
    private String model;

    @Column(name = "prompt_version")
    private String promptVersion;

    @Column(name = "input_hash")
    private String inputHash;

    @Column(name = "output_hash")
    private String outputHash;

    @Column(name = "estimated_input_tokens")
    private Integer estimatedInputTokens;

    @Column(name = "estimated_output_tokens")
    private Integer estimatedOutputTokens;

    @Column(name = "cost_estimate")
    private Double costEstimate;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at", nullable = false)
    private Instant finishedAt;
}
