package cl.gradeops.ai.api.assessment.application.port.out;

import cl.gradeops.ai.api.assessment.domain.model.AiOperation;
import cl.gradeops.ai.api.assessment.domain.model.AiOperationType;
import cl.gradeops.ai.api.assessment.domain.model.AssessmentId;

import java.util.Optional;
import java.util.UUID;

public interface AiOperationRepositoryPort {
    void save(AiOperation operation);
    Optional<AiOperation> findById(UUID id);

    /**
     * The most recently created {@code AiOperation} for this (assessmentId, operationType) pair
     * — the single authoritative row {@code GET .../generation-status} and {@code POST
     * .../draft/retry} both read, per LOCAL-CONTRACTS.md (never {@code MAX(version_number)}-style
     * derivation; there is exactly one row that matters here since {@code
     * uq_ai_operations_in_flight} already guarantees at most one PENDING/IN_PROGRESS row at a
     * time, and retry always reuses that same logical operation rather than creating a new one).
     */
    Optional<AiOperation> findLatestByAssessmentIdAndOperationType(AssessmentId assessmentId, AiOperationType operationType);
}
