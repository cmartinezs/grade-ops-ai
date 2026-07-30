package cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web.response;

import java.util.UUID;

/**
 * {@code POST .../draft/retry}'s {@code 202} body — a snapshot of the retried {@code AiOperation}.
 * Per Authoring Operation Contract § 3, Web never parses this body to decide what happened next;
 * it always re-polls {@code GET .../generation-status} afterward.
 */
public record AiOperationResponse(
    UUID id,
    String operationType,
    String status,
    String failureCode,
    boolean retryable,
    UUID resultRevisionId
) {}
