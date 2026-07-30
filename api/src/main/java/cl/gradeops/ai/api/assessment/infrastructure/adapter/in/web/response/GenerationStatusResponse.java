package cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web.response;

import java.util.UUID;

/**
 * {@code GET .../generation-status}'s {@code 200} body. {@code status} values: {@code
 * NOT_STARTED}, {@code SUCCEEDED}, {@code IN_PROGRESS}, {@code INDETERMINATE}, {@code
 * FAILED_RETRYABLE}, {@code FAILED_TERMINAL} — see LOCAL-CONTRACTS.md § Canonical status
 * taxonomy and API-A3-HANDOFF.md for the {@code SUCCEEDED}/{@code FAILED_TERMINAL} additions this
 * session made to close a taxonomy gap.
 */
public record GenerationStatusResponse(
    String operationType,
    String status,
    String failureCode,
    boolean retryable,
    UUID currentRevisionId
) {}
