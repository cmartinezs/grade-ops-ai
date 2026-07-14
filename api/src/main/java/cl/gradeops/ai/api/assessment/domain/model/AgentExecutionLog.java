package cl.gradeops.ai.api.assessment.domain.model;

import cl.gradeops.ai.api.shared.domain.exception.DomainInvariantViolationException;
import cl.gradeops.ai.api.shared.domain.model.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

public class AgentExecutionLog extends AggregateRoot<UUID> {

    private UUID id;
    private AssessmentId assessmentId;
    private UUID draftId;
    private UUID agentExecutionId;
    private String agentName;
    private String provider;
    private String model;
    private String promptVersion;
    private String inputHash;
    private String outputHash;
    private Integer estimatedInputTokens;
    private Integer estimatedOutputTokens;
    private Double costEstimate;
    private String status;
    private String errorCode;
    private Instant startedAt;
    private Instant finishedAt;

    private AgentExecutionLog() {}

    /**
     * First persisted state of an execution log (success or failure) — {@code draftId} is always
     * null here, since the draft row (success path only) is created afterward and cross-referenced
     * via {@link #withDraftId(UUID)}.
     */
    public static AgentExecutionLog create(AssessmentId assessmentId, UUID agentExecutionId, String agentName,
                                            String provider, String model, String promptVersion,
                                            String inputHash, String outputHash,
                                            Integer estimatedInputTokens, Integer estimatedOutputTokens,
                                            Double costEstimate, String status, String errorCode,
                                            Instant startedAt, Instant finishedAt) {
        validate(assessmentId, status, startedAt, finishedAt);
        AgentExecutionLog log = new AgentExecutionLog();
        log.id = UUID.randomUUID();
        log.assessmentId = assessmentId;
        log.draftId = null;
        log.agentExecutionId = agentExecutionId;
        log.agentName = agentName;
        log.provider = provider;
        log.model = model;
        log.promptVersion = promptVersion;
        log.inputHash = inputHash;
        log.outputHash = outputHash;
        log.estimatedInputTokens = estimatedInputTokens;
        log.estimatedOutputTokens = estimatedOutputTokens;
        log.costEstimate = costEstimate;
        log.status = status;
        log.errorCode = errorCode;
        log.startedAt = startedAt;
        log.finishedAt = finishedAt;
        return log;
    }

    /** Cross-references this log with the draft row it produced (success path only). */
    public AgentExecutionLog withDraftId(UUID draftId) {
        return restore(id, assessmentId, draftId, agentExecutionId, agentName, provider, model, promptVersion,
                inputHash, outputHash, estimatedInputTokens, estimatedOutputTokens, costEstimate, status,
                errorCode, startedAt, finishedAt);
    }

    public static AgentExecutionLog restore(UUID id, AssessmentId assessmentId, UUID draftId, UUID agentExecutionId,
                                             String agentName, String provider, String model, String promptVersion,
                                             String inputHash, String outputHash, Integer estimatedInputTokens,
                                             Integer estimatedOutputTokens, Double costEstimate, String status,
                                             String errorCode, Instant startedAt, Instant finishedAt) {
        if (id == null) throw new DomainInvariantViolationException("id must not be null");
        validate(assessmentId, status, startedAt, finishedAt);
        AgentExecutionLog log = new AgentExecutionLog();
        log.id = id;
        log.assessmentId = assessmentId;
        log.draftId = draftId;
        log.agentExecutionId = agentExecutionId;
        log.agentName = agentName;
        log.provider = provider;
        log.model = model;
        log.promptVersion = promptVersion;
        log.inputHash = inputHash;
        log.outputHash = outputHash;
        log.estimatedInputTokens = estimatedInputTokens;
        log.estimatedOutputTokens = estimatedOutputTokens;
        log.costEstimate = costEstimate;
        log.status = status;
        log.errorCode = errorCode;
        log.startedAt = startedAt;
        log.finishedAt = finishedAt;
        return log;
    }

    private static void validate(AssessmentId assessmentId, String status, Instant startedAt, Instant finishedAt) {
        if (assessmentId == null) throw new DomainInvariantViolationException("assessmentId must not be null");
        if (status == null || status.isBlank()) throw new DomainInvariantViolationException("status must not be blank");
        if (startedAt == null) throw new DomainInvariantViolationException("startedAt must not be null");
        if (finishedAt == null) throw new DomainInvariantViolationException("finishedAt must not be null");
    }

    @Override protected UUID id()                    { return id; }
    public UUID getId()                               { return id; }
    public AssessmentId getAssessmentId()             { return assessmentId; }
    public UUID getDraftId()                          { return draftId; }
    public UUID getAgentExecutionId()                 { return agentExecutionId; }
    public String getAgentName()                      { return agentName; }
    public String getProvider()                       { return provider; }
    public String getModel()                          { return model; }
    public String getPromptVersion()                  { return promptVersion; }
    public String getInputHash()                      { return inputHash; }
    public String getOutputHash()                     { return outputHash; }
    public Integer getEstimatedInputTokens()          { return estimatedInputTokens; }
    public Integer getEstimatedOutputTokens()         { return estimatedOutputTokens; }
    public Double getCostEstimate()                   { return costEstimate; }
    public String getStatus()                         { return status; }
    public String getErrorCode()                      { return errorCode; }
    public Instant getStartedAt()                     { return startedAt; }
    public Instant getFinishedAt()                    { return finishedAt; }
}
