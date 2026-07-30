package cl.gradeops.ai.api.assessment.application.result;

import java.util.UUID;

/**
 * The public shape of {@code POST .../draft/retry}'s {@code 202} body — a stable projection of
 * {@code AiOperation}, never the JPA entity itself. {@code status} uses {@code AiOperation}'s own
 * internal taxonomy (PENDING/IN_PROGRESS/SUCCEEDED/FAILED_RETRYABLE/FAILED_TERMINAL), distinct
 * from {@code generation-status}'s simplified public taxonomy — Web is documented to not branch
 * on this body at all and instead re-poll {@code generation-status} for the authoritative state.
 */
public record RetryGenerationResult(
    UUID id,
    String operationType,
    String status,
    String failureCode,
    boolean retryable,
    UUID resultRevisionId
) {}
