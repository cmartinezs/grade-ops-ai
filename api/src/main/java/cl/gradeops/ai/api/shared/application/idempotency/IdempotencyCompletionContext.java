package cl.gradeops.ai.api.shared.application.idempotency;

/**
 * Everything needed to write an {@link IdempotencyRecord} that is known before an AI dispatch
 * begins — bundled so a coordinator can write the record from inside its own Phase 2 transaction
 * (atomically with the durable outcome it describes) instead of the caller writing it afterward
 * in a separate transaction. {@code resultReference}/{@code responseStatus} are supplied
 * separately at completion time, since they are only known once the outcome (success or durable
 * failure) is determined.
 */
public record IdempotencyCompletionContext(
        IdempotencyScope scope, String operationType, String idempotencyKey, String requestPayloadHash) {
}
