package cl.gradeops.ai.api.assessment.application.result;

import java.util.UUID;

/**
 * The public shape of {@code GET .../generation-status}, per LOCAL-CONTRACTS.md § API ↔ Web
 * public contract: {@code { operationType, status, failureCode?, retryable, currentRevisionId? }}.
 *
 * <p>{@code status} taxonomy: the four explicitly enumerated values (NOT_STARTED, IN_PROGRESS,
 * FAILED_RETRYABLE, INDETERMINATE) plus two this session had to fill in — SUCCEEDED (derived from
 * {@code currentRevisionId} being non-null, per the taxonomy table's own "(plus implicit success
 * via currentRevisionId non-null)" note) and FAILED_TERMINAL (a real {@code AiOperation} state
 * the table's four-value list omits; surfaced honestly rather than mislabeled as
 * FAILED_RETRYABLE, since that would incorrectly imply retry is possible). Both additions are
 * documented prominently in API-A3-HANDOFF.md for Session D/A4 to confirm or correct.
 */
public record GetGenerationStatusResult(
    String operationType,
    String status,
    String failureCode,
    boolean retryable,
    UUID currentRevisionId
) {}
