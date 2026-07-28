package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agent_attempts")
@Getter
@Setter
@NoArgsConstructor
public class AgentAttemptJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "ai_operation_id", nullable = false)
    private UUID aiOperationId;

    @Column(name = "attempt_number", nullable = false)
    private int attemptNumber;

    @Column(name = "agent_name", nullable = false)
    private String agentName;

    @Column(name = "resolved_provider")
    private String resolvedProvider;

    @Column(name = "resolved_model")
    private String resolvedModel;

    @Column(name = "prompt_version")
    private String promptVersion;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "dispatched_at", nullable = false)
    private Instant dispatchedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "provider_request_id")
    private String providerRequestId;

    @Column(name = "failure_code")
    private String failureCode;

    @Column(name = "estimated_input_tokens")
    private Integer estimatedInputTokens;

    @Column(name = "estimated_output_tokens")
    private Integer estimatedOutputTokens;

    @Column(name = "cost_estimate", precision = 12, scale = 6)
    private BigDecimal costEstimate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "structured_result", columnDefinition = "jsonb")
    private String structuredResult;
}
