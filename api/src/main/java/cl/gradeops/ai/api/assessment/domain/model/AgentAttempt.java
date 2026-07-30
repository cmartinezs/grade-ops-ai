package cl.gradeops.ai.api.assessment.domain.model;

import cl.gradeops.ai.api.shared.domain.exception.DomainInvariantViolationException;
import cl.gradeops.ai.api.shared.domain.model.AggregateRoot;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * One concrete dispatch under an {@link AiOperation}: resolved provider/model, prompt version,
 * correlation id, timing, outcome and cost. {@link #markCompleted}/{@link #markFailed} are only
 * valid from {@link AgentAttemptStatus#DISPATCHED} — once {@code COMPLETED} or {@code FAILED},
 * an attempt accepts no further transition (a user-initiated retry creates a new attempt instead).
 */
public class AgentAttempt extends AggregateRoot<UUID> {

    private UUID id;
    private UUID aiOperationId;
    private int attemptNumber;
    private String agentName;
    private String resolvedProvider;
    private String resolvedModel;
    private String promptVersion;
    private String correlationId;
    private Instant dispatchedAt;
    private Instant completedAt;
    private AgentAttemptStatus status;
    private String providerRequestId;
    private String failureCode;
    private Integer estimatedInputTokens;
    private Integer estimatedOutputTokens;
    private BigDecimal costEstimate;
    private String structuredResult;

    private AgentAttempt() {}

    public static AgentAttempt dispatch(UUID aiOperationId, int attemptNumber, String agentName,
                                         String promptVersion, String correlationId) {
        if (aiOperationId == null) throw new DomainInvariantViolationException("aiOperationId must not be null");
        if (attemptNumber < 1) throw new DomainInvariantViolationException("attemptNumber must be >= 1");
        if (agentName == null || agentName.isBlank()) throw new DomainInvariantViolationException("agentName must not be blank");
        AgentAttempt a = new AgentAttempt();
        a.id = UUID.randomUUID();
        a.aiOperationId = aiOperationId;
        a.attemptNumber = attemptNumber;
        a.agentName = agentName;
        a.resolvedProvider = null;
        a.resolvedModel = null;
        a.promptVersion = promptVersion;
        a.correlationId = correlationId;
        a.dispatchedAt = Instant.now();
        a.completedAt = null;
        a.status = AgentAttemptStatus.DISPATCHED;
        a.providerRequestId = null;
        a.failureCode = null;
        a.estimatedInputTokens = null;
        a.estimatedOutputTokens = null;
        a.costEstimate = null;
        a.structuredResult = null;
        return a;
    }

    public static AgentAttempt restore(UUID id, UUID aiOperationId, int attemptNumber, String agentName,
                                        String resolvedProvider, String resolvedModel, String promptVersion,
                                        String correlationId, Instant dispatchedAt, Instant completedAt,
                                        AgentAttemptStatus status, String providerRequestId, String failureCode,
                                        Integer estimatedInputTokens, Integer estimatedOutputTokens,
                                        BigDecimal costEstimate, String structuredResult) {
        if (id == null) throw new DomainInvariantViolationException("id must not be null");
        if (aiOperationId == null) throw new DomainInvariantViolationException("aiOperationId must not be null");
        if (attemptNumber < 1) throw new DomainInvariantViolationException("attemptNumber must be >= 1");
        if (agentName == null || agentName.isBlank()) throw new DomainInvariantViolationException("agentName must not be blank");
        if (status == null) throw new DomainInvariantViolationException("status must not be null");
        if (dispatchedAt == null) throw new DomainInvariantViolationException("dispatchedAt must not be null");
        AgentAttempt a = new AgentAttempt();
        a.id = id;
        a.aiOperationId = aiOperationId;
        a.attemptNumber = attemptNumber;
        a.agentName = agentName;
        a.resolvedProvider = resolvedProvider;
        a.resolvedModel = resolvedModel;
        a.promptVersion = promptVersion;
        a.correlationId = correlationId;
        a.dispatchedAt = dispatchedAt;
        a.completedAt = completedAt;
        a.status = status;
        a.providerRequestId = providerRequestId;
        a.failureCode = failureCode;
        a.estimatedInputTokens = estimatedInputTokens;
        a.estimatedOutputTokens = estimatedOutputTokens;
        a.costEstimate = costEstimate;
        a.structuredResult = structuredResult;
        return a;
    }

    /** DISPATCHED → COMPLETED, recording the actually-resolved provider/model — never null once completed. */
    public AgentAttempt markCompleted(String resolvedProvider, String resolvedModel, String providerRequestId,
                                       Integer estimatedInputTokens, Integer estimatedOutputTokens,
                                       BigDecimal costEstimate, String structuredResult) {
        if (status != AgentAttemptStatus.DISPATCHED) {
            throw new DomainInvariantViolationException("cannot transition to COMPLETED from " + status);
        }
        if (resolvedProvider == null || resolvedProvider.isBlank())
            throw new DomainInvariantViolationException("resolvedProvider must not be blank on a completed attempt");
        if (resolvedModel == null || resolvedModel.isBlank())
            throw new DomainInvariantViolationException("resolvedModel must not be blank on a completed attempt");
        AgentAttempt a = copy();
        a.resolvedProvider = resolvedProvider;
        a.resolvedModel = resolvedModel;
        a.providerRequestId = providerRequestId;
        a.estimatedInputTokens = estimatedInputTokens;
        a.estimatedOutputTokens = estimatedOutputTokens;
        a.costEstimate = costEstimate;
        a.structuredResult = structuredResult;
        a.status = AgentAttemptStatus.COMPLETED;
        a.completedAt = Instant.now();
        return a;
    }

    /**
     * DISPATCHED → FAILED, retaining {@code structuredResult} when a response arrived before the
     * failure. {@code resolvedProvider}/{@code resolvedModel} are {@code null} for a failure that
     * occurred before a provider was ever resolved (e.g. transport-level {@code
     * AGENT_UNAVAILABLE}, or agents-side {@code INVALID_COMMAND}); they must be supplied,
     * non-blank, when the caller knows a provider was resolved before the failure (e.g. {@code
     * MALFORMED_OUTPUT}, or a late-arriving successful response that failed only the CAS check at
     * persist time — {@code STALE_ON_COMPLETION}). This is a widened signature, not a second
     * overload, so no call site can accidentally keep discarding known provider/model evidence.
     */
    public AgentAttempt markFailed(String failureCode, String resolvedProvider, String resolvedModel,
                                    String structuredResult) {
        if (status != AgentAttemptStatus.DISPATCHED) {
            throw new DomainInvariantViolationException("cannot transition to FAILED from " + status);
        }
        if (failureCode == null || failureCode.isBlank())
            throw new DomainInvariantViolationException("failureCode must not be blank");
        if (resolvedProvider != null && resolvedProvider.isBlank())
            throw new DomainInvariantViolationException("resolvedProvider must not be blank if provided");
        if (resolvedModel != null && resolvedModel.isBlank())
            throw new DomainInvariantViolationException("resolvedModel must not be blank if provided");
        AgentAttempt a = copy();
        a.failureCode = failureCode;
        a.resolvedProvider = resolvedProvider;
        a.resolvedModel = resolvedModel;
        a.structuredResult = structuredResult;
        a.status = AgentAttemptStatus.FAILED;
        a.completedAt = Instant.now();
        return a;
    }

    private AgentAttempt copy() {
        AgentAttempt a = new AgentAttempt();
        a.id = id;
        a.aiOperationId = aiOperationId;
        a.attemptNumber = attemptNumber;
        a.agentName = agentName;
        a.resolvedProvider = resolvedProvider;
        a.resolvedModel = resolvedModel;
        a.promptVersion = promptVersion;
        a.correlationId = correlationId;
        a.dispatchedAt = dispatchedAt;
        a.completedAt = completedAt;
        a.status = status;
        a.providerRequestId = providerRequestId;
        a.failureCode = failureCode;
        a.estimatedInputTokens = estimatedInputTokens;
        a.estimatedOutputTokens = estimatedOutputTokens;
        a.costEstimate = costEstimate;
        a.structuredResult = structuredResult;
        return a;
    }

    @Override protected UUID id()                          { return id; }
    public UUID getId()                                     { return id; }
    public UUID getAiOperationId()                          { return aiOperationId; }
    public int getAttemptNumber()                           { return attemptNumber; }
    public String getAgentName()                            { return agentName; }
    public String getResolvedProvider()                     { return resolvedProvider; }
    public String getResolvedModel()                        { return resolvedModel; }
    public String getPromptVersion()                        { return promptVersion; }
    public String getCorrelationId()                        { return correlationId; }
    public Instant getDispatchedAt()                        { return dispatchedAt; }
    public Instant getCompletedAt()                         { return completedAt; }
    public AgentAttemptStatus getStatus()                   { return status; }
    public String getProviderRequestId()                    { return providerRequestId; }
    public String getFailureCode()                          { return failureCode; }
    public Integer getEstimatedInputTokens()                { return estimatedInputTokens; }
    public Integer getEstimatedOutputTokens()               { return estimatedOutputTokens; }
    public BigDecimal getCostEstimate()                     { return costEstimate; }
    public String getStructuredResult()                     { return structuredResult; }
}
