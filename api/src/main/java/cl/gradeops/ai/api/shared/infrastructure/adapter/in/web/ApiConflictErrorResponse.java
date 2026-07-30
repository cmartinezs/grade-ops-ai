package cl.gradeops.ai.api.shared.infrastructure.adapter.in.web;

/**
 * Uniform {@code 409} body for every typed authoring-operation conflict (Authoring Operation
 * Contract § Typed, stable error codes): {@code IDEMPOTENCY_KEY_PAYLOAD_MISMATCH}, {@code
 * ALREADY_GENERATED}, {@code STALE_REVISION}, {@code STALE_ON_COMPLETION}, {@code
 * NO_ACTIVE_OPERATION_TO_RETRY}, {@code OPERATION_IN_PROGRESS}. Deliberately a distinct type
 * from {@link ApiErrorResponse} (field {@code code}, not {@code error}) — Web branches on {@code
 * code} for these conflicts specifically (LOCAL-CONTRACTS.md's literal "{ code, message }"
 * wording); other, pre-existing error codes outside the authoring-operation family keep {@link
 * ApiErrorResponse}'s shape unchanged.
 */
public record ApiConflictErrorResponse(String code, String message) {
    public static ApiConflictErrorResponse of(String code, String message) {
        return new ApiConflictErrorResponse(code, message);
    }
}
