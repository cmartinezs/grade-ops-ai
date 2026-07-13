package cl.gradeops.ai.api.assessment.domain.model;

import cl.gradeops.ai.api.shared.domain.exception.DomainInvariantViolationException;
import cl.gradeops.ai.api.shared.domain.model.AggregateRoot;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AssessmentDraft extends AggregateRoot<UUID> {

    private UUID id;
    private AssessmentId assessmentId;
    private int versionNumber;
    private UUID previousVersionId;
    private String title;
    private String context;
    private String instructions;
    private List<String> objectives;
    private List<String> deliverables;
    private List<String> constraints;
    private UUID agentExecutionLogId;
    private Instant createdAt;

    private AssessmentDraft() {}

    /** First version of a draft for an assessment (US-011, initial generation). */
    public static AssessmentDraft generate(AssessmentId assessmentId, String title, String context,
                                            String instructions, List<String> objectives,
                                            List<String> deliverables, List<String> constraints,
                                            UUID agentExecutionLogId) {
        validateContent(assessmentId, title, context, instructions, objectives, deliverables, constraints);
        AssessmentDraft d = new AssessmentDraft();
        d.id = UUID.randomUUID();
        d.assessmentId = assessmentId;
        d.versionNumber = 1;
        d.previousVersionId = null;
        d.title = title;
        d.context = context;
        d.instructions = instructions;
        d.objectives = objectives;
        d.deliverables = deliverables;
        d.constraints = constraints;
        d.agentExecutionLogId = agentExecutionLogId;
        d.createdAt = Instant.now();
        return d;
    }

    /**
     * A new, non-destructive version of an existing draft (US-012, regeneration).
     * The previous version's row is never modified — this always produces a new row
     * linked to it via {@code previousVersionId}.
     */
    public static AssessmentDraft regenerate(AssessmentDraft previousVersion, String title, String context,
                                              String instructions, List<String> objectives,
                                              List<String> deliverables, List<String> constraints,
                                              UUID agentExecutionLogId) {
        if (previousVersion == null) throw new DomainInvariantViolationException("previousVersion must not be null");
        validateContent(previousVersion.assessmentId, title, context, instructions, objectives, deliverables, constraints);
        AssessmentDraft d = new AssessmentDraft();
        d.id = UUID.randomUUID();
        d.assessmentId = previousVersion.assessmentId;
        d.versionNumber = previousVersion.versionNumber + 1;
        d.previousVersionId = previousVersion.id;
        d.title = title;
        d.context = context;
        d.instructions = instructions;
        d.objectives = objectives;
        d.deliverables = deliverables;
        d.constraints = constraints;
        d.agentExecutionLogId = agentExecutionLogId;
        d.createdAt = Instant.now();
        return d;
    }

    public static AssessmentDraft restore(UUID id, AssessmentId assessmentId, int versionNumber,
                                           UUID previousVersionId, String title, String context,
                                           String instructions, List<String> objectives,
                                           List<String> deliverables, List<String> constraints,
                                           UUID agentExecutionLogId, Instant createdAt) {
        if (id == null) throw new DomainInvariantViolationException("id must not be null");
        validateContent(assessmentId, title, context, instructions, objectives, deliverables, constraints);
        if (versionNumber < 1) throw new DomainInvariantViolationException("versionNumber must be >= 1");
        if (versionNumber == 1 && previousVersionId != null)
            throw new DomainInvariantViolationException("version 1 must not have a previousVersionId");
        if (versionNumber > 1 && previousVersionId == null)
            throw new DomainInvariantViolationException("versionNumber > 1 must have a previousVersionId");
        if (createdAt == null) throw new DomainInvariantViolationException("createdAt must not be null");
        AssessmentDraft d = new AssessmentDraft();
        d.id = id;
        d.assessmentId = assessmentId;
        d.versionNumber = versionNumber;
        d.previousVersionId = previousVersionId;
        d.title = title;
        d.context = context;
        d.instructions = instructions;
        d.objectives = objectives;
        d.deliverables = deliverables;
        d.constraints = constraints;
        d.agentExecutionLogId = agentExecutionLogId;
        d.createdAt = createdAt;
        return d;
    }

    private static void validateContent(AssessmentId assessmentId, String title, String context,
                                         String instructions, List<String> objectives,
                                         List<String> deliverables, List<String> constraints) {
        if (assessmentId == null) throw new DomainInvariantViolationException("assessmentId must not be null");
        if (title == null || title.isBlank())               throw new DomainInvariantViolationException("title must not be blank");
        if (context == null || context.isBlank())            throw new DomainInvariantViolationException("context must not be blank");
        if (instructions == null || instructions.isBlank())  throw new DomainInvariantViolationException("instructions must not be blank");
        if (objectives == null)   throw new DomainInvariantViolationException("objectives must not be null");
        if (deliverables == null) throw new DomainInvariantViolationException("deliverables must not be null");
        if (constraints == null)  throw new DomainInvariantViolationException("constraints must not be null");
    }

    @Override protected UUID id()              { return id; }
    public UUID getId()                        { return id; }
    public AssessmentId getAssessmentId()      { return assessmentId; }
    public int getVersionNumber()              { return versionNumber; }
    public UUID getPreviousVersionId()         { return previousVersionId; }
    public String getTitle()                   { return title; }
    public String getContext()                 { return context; }
    public String getInstructions()            { return instructions; }
    public List<String> getObjectives()        { return objectives; }
    public List<String> getDeliverables()      { return deliverables; }
    public List<String> getConstraints()       { return constraints; }
    public UUID getAgentExecutionLogId()       { return agentExecutionLogId; }
    public Instant getCreatedAt()              { return createdAt; }
}
