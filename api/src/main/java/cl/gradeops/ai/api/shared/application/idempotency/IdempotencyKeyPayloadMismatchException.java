package cl.gradeops.ai.api.shared.application.idempotency;

import cl.gradeops.ai.api.shared.application.exception.ApplicationException;

/** Same idempotency key reused with a different, semantically-changed request payload. */
public class IdempotencyKeyPayloadMismatchException extends ApplicationException {
    public IdempotencyKeyPayloadMismatchException(String idempotencyKey) {
        super("Idempotency-Key '" + idempotencyKey + "' was already used with a different request payload");
    }
}
