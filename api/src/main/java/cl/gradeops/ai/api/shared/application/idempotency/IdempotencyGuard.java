package cl.gradeops.ai.api.shared.application.idempotency;

import java.util.Optional;

/**
 * Reusable idempotency-check/record service, usable by any command handler.
 *
 * <p>{@link #check} distinguishes the three-way contract: no prior record → {@code
 * Optional.empty()} (proceed); same key + same payload hash → the prior record (replay); same
 * key + different payload hash → throws {@link IdempotencyKeyPayloadMismatchException}.
 */
public class IdempotencyGuard {

    private final IdempotencyRepositoryPort repository;

    public IdempotencyGuard(IdempotencyRepositoryPort repository) {
        this.repository = repository;
    }

    public Optional<IdempotencyRecord> check(IdempotencyScope scope, String operationType,
                                              String idempotencyKey, String requestPayloadHash) {
        Optional<IdempotencyRecord> existing = repository.find(scope, operationType, idempotencyKey);
        if (existing.isEmpty()) {
            return Optional.empty();
        }
        IdempotencyRecord record = existing.get();
        if (!record.getRequestPayloadHash().equals(requestPayloadHash)) {
            throw new IdempotencyKeyPayloadMismatchException(idempotencyKey);
        }
        return existing;
    }

    public void record(IdempotencyScope scope, String operationType, String idempotencyKey,
                        String requestPayloadHash, String resultReference, Integer responseStatus) {
        repository.save(IdempotencyRecord.create(
                scope, operationType, idempotencyKey, requestPayloadHash, resultReference, responseStatus));
    }
}
