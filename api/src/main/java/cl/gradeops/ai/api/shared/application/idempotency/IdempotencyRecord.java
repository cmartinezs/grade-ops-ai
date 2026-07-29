package cl.gradeops.ai.api.shared.application.idempotency;

import cl.gradeops.ai.api.shared.domain.exception.DomainInvariantViolationException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * A repository-port-backed value, not an {@code AggregateRoot} — see LOCAL-CONTRACTS.md §
 * {@code IdempotencyRecord}. Retention is 24h from {@code createdAt}; no cleanup job this cut.
 */
public final class IdempotencyRecord {

    private static final long RETENTION_HOURS = 24;

    private final UUID id;
    private final IdempotencyScope scope;
    private final String operationType;
    private final String idempotencyKey;
    private final String requestPayloadHash;
    private final String resultReference;
    private final Integer responseStatus;
    private final Instant createdAt;
    private final Instant expiresAt;

    private IdempotencyRecord(UUID id, IdempotencyScope scope, String operationType, String idempotencyKey,
                               String requestPayloadHash, String resultReference, Integer responseStatus,
                               Instant createdAt, Instant expiresAt) {
        this.id = id;
        this.scope = scope;
        this.operationType = operationType;
        this.idempotencyKey = idempotencyKey;
        this.requestPayloadHash = requestPayloadHash;
        this.resultReference = resultReference;
        this.responseStatus = responseStatus;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public static IdempotencyRecord create(IdempotencyScope scope, String operationType, String idempotencyKey,
                                            String requestPayloadHash, String resultReference, Integer responseStatus) {
        validate(scope, operationType, idempotencyKey, requestPayloadHash);
        Instant now = Instant.now();
        return new IdempotencyRecord(UUID.randomUUID(), scope, operationType, idempotencyKey, requestPayloadHash,
                resultReference, responseStatus, now, now.plus(RETENTION_HOURS, ChronoUnit.HOURS));
    }

    public static IdempotencyRecord restore(UUID id, IdempotencyScope scope, String operationType, String idempotencyKey,
                                             String requestPayloadHash, String resultReference, Integer responseStatus,
                                             Instant createdAt, Instant expiresAt) {
        if (id == null) throw new DomainInvariantViolationException("id must not be null");
        validate(scope, operationType, idempotencyKey, requestPayloadHash);
        if (createdAt == null) throw new DomainInvariantViolationException("createdAt must not be null");
        if (expiresAt == null) throw new DomainInvariantViolationException("expiresAt must not be null");
        return new IdempotencyRecord(id, scope, operationType, idempotencyKey, requestPayloadHash,
                resultReference, responseStatus, createdAt, expiresAt);
    }

    private static void validate(IdempotencyScope scope, String operationType, String idempotencyKey, String requestPayloadHash) {
        if (scope == null) throw new DomainInvariantViolationException("scope must not be null");
        if (operationType == null || operationType.isBlank())
            throw new DomainInvariantViolationException("operationType must not be blank");
        if (idempotencyKey == null || idempotencyKey.isBlank())
            throw new DomainInvariantViolationException("idempotencyKey must not be blank");
        if (requestPayloadHash == null || requestPayloadHash.isBlank())
            throw new DomainInvariantViolationException("requestPayloadHash must not be blank");
    }

    public UUID getId()                    { return id; }
    public IdempotencyScope getScope()     { return scope; }
    public String getOperationType()       { return operationType; }
    public String getIdempotencyKey()      { return idempotencyKey; }
    public String getRequestPayloadHash()  { return requestPayloadHash; }
    public String getResultReference()     { return resultReference; }
    public Integer getResponseStatus()     { return responseStatus; }
    public Instant getCreatedAt()          { return createdAt; }
    public Instant getExpiresAt()          { return expiresAt; }
}
