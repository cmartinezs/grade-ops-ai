package cl.gradeops.ai.api.assessment.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_operations")
@Getter
@Setter
@NoArgsConstructor
public class AiOperationJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "assessment_id", nullable = false)
    private UUID assessmentId;

    @Column(name = "operation_type", nullable = false)
    private String operationType;

    @Column(name = "requested_by", nullable = false)
    private String requestedBy;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "expected_revision_id")
    private UUID expectedRevisionId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "result_revision_id")
    private UUID resultRevisionId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
