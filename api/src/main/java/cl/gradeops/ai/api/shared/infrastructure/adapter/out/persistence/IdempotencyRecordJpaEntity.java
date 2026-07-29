package cl.gradeops.ai.api.shared.infrastructure.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_records")
@Getter
@Setter
@NoArgsConstructor
public class IdempotencyRecordJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "scope_type", nullable = false)
    private String scopeType;

    @Column(name = "teacher_uid")
    private String teacherUid;

    @Column(name = "assessment_id")
    private UUID assessmentId;

    @Column(name = "operation_type", nullable = false)
    private String operationType;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "request_payload_hash", nullable = false)
    private String requestPayloadHash;

    @Column(name = "result_reference")
    private String resultReference;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
