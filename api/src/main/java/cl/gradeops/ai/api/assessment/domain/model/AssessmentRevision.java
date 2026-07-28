package cl.gradeops.ai.api.assessment.domain.model;

import cl.gradeops.ai.api.shared.domain.exception.DomainInvariantViolationException;
import cl.gradeops.ai.api.shared.domain.model.AggregateRoot;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * One immutable snapshot of authored content (US-Authoring). Unlike {@code AssessmentDraft},
 * there is no in-place edit path — every accepted content change, AI-generated or human-edited,
 * is a new row chained to its predecessor via {@code previousRevisionId}.
 */
public class AssessmentRevision extends AggregateRoot<UUID> {

    private UUID id;
    private AssessmentId assessmentId;
    private int versionNumber;
    private UUID previousRevisionId;
    private RevisionOrigin origin;
    private String actorId;
    private String reason;
    private UUID sourceAgentAttemptId;
    private String title;
    private String context;
    private String instructions;
    private List<String> objectives;
    private List<String> deliverables;
    private List<String> constraints;
    private Instant createdAt;

    private AssessmentRevision() {}

    /** Initial AI-generated revision for an assessment (version 1, no predecessor). */
    public static AssessmentRevision generateFromAi(AssessmentId assessmentId, String title, String context,
                                                      String instructions, List<String> objectives,
                                                      List<String> deliverables, List<String> constraints,
                                                      String actorId, UUID sourceAgentAttemptId) {
        validateContent(assessmentId, title, context, instructions, objectives, deliverables, constraints);
        validateActorId(actorId);
        validateSourceAgentAttemptId(sourceAgentAttemptId);
        AssessmentRevision r = new AssessmentRevision();
        r.id = UUID.randomUUID();
        r.assessmentId = assessmentId;
        r.versionNumber = 1;
        r.previousRevisionId = null;
        r.origin = RevisionOrigin.AI_GENERATED;
        r.actorId = actorId;
        r.reason = null;
        r.sourceAgentAttemptId = sourceAgentAttemptId;
        r.title = title;
        r.context = context;
        r.instructions = instructions;
        r.objectives = List.copyOf(objectives);
        r.deliverables = List.copyOf(deliverables);
        r.constraints = List.copyOf(constraints);
        r.createdAt = Instant.now();
        return r;
    }

    /**
     * A new AI-regenerated revision chained after {@code previousRevision} — never mutates it.
     * {@code reason} carries the regeneration's adjustment notes.
     */
    public static AssessmentRevision regenerateFromAi(AssessmentRevision previousRevision, String title, String context,
                                                        String instructions, List<String> objectives,
                                                        List<String> deliverables, List<String> constraints,
                                                        String actorId, String reason, UUID sourceAgentAttemptId) {
        if (previousRevision == null) throw new DomainInvariantViolationException("previousRevision must not be null");
        validateContent(previousRevision.assessmentId, title, context, instructions, objectives, deliverables, constraints);
        validateActorId(actorId);
        validateSourceAgentAttemptId(sourceAgentAttemptId);
        AssessmentRevision r = new AssessmentRevision();
        r.id = UUID.randomUUID();
        r.assessmentId = previousRevision.assessmentId;
        r.versionNumber = previousRevision.versionNumber + 1;
        r.previousRevisionId = previousRevision.id;
        r.origin = RevisionOrigin.AI_GENERATED;
        r.actorId = actorId;
        r.reason = reason;
        r.sourceAgentAttemptId = sourceAgentAttemptId;
        r.title = title;
        r.context = context;
        r.instructions = instructions;
        r.objectives = List.copyOf(objectives);
        r.deliverables = List.copyOf(deliverables);
        r.constraints = List.copyOf(constraints);
        r.createdAt = Instant.now();
        return r;
    }

    /**
     * A new human-edited revision chained after {@code previousRevision} — never mutates it,
     * and never overwrites the AI-generated content that came before. The AI-generated revision
     * this edit follows remains readable forever, closing the provenance gap
     * {@code AssessmentDraft.applyEdit} left open.
     */
    public static AssessmentRevision createFromHumanEdit(AssessmentRevision previousRevision, String title, String context,
                                                           String instructions, List<String> objectives,
                                                           List<String> deliverables, List<String> constraints,
                                                           String actorId, String reason) {
        if (previousRevision == null) throw new DomainInvariantViolationException("previousRevision must not be null");
        validateContent(previousRevision.assessmentId, title, context, instructions, objectives, deliverables, constraints);
        validateActorId(actorId);
        AssessmentRevision r = new AssessmentRevision();
        r.id = UUID.randomUUID();
        r.assessmentId = previousRevision.assessmentId;
        r.versionNumber = previousRevision.versionNumber + 1;
        r.previousRevisionId = previousRevision.id;
        r.origin = RevisionOrigin.HUMAN_EDITED;
        r.actorId = actorId;
        r.reason = reason;
        r.sourceAgentAttemptId = null;
        r.title = title;
        r.context = context;
        r.instructions = instructions;
        r.objectives = List.copyOf(objectives);
        r.deliverables = List.copyOf(deliverables);
        r.constraints = List.copyOf(constraints);
        r.createdAt = Instant.now();
        return r;
    }

    /**
     * Full-field reconstruction for persistence mapping — the only path able to produce
     * {@link RevisionOrigin#LEGACY_UNKNOWN}, which the legacy backfill migration uses directly.
     */
    public static AssessmentRevision restore(UUID id, AssessmentId assessmentId, int versionNumber, UUID previousRevisionId,
                                              RevisionOrigin origin, String actorId, String reason, UUID sourceAgentAttemptId,
                                              String title, String context, String instructions,
                                              List<String> objectives, List<String> deliverables, List<String> constraints,
                                              Instant createdAt) {
        if (id == null) throw new DomainInvariantViolationException("id must not be null");
        validateContent(assessmentId, title, context, instructions, objectives, deliverables, constraints);
        if (origin == null) throw new DomainInvariantViolationException("origin must not be null");
        if (versionNumber < 1) throw new DomainInvariantViolationException("versionNumber must be >= 1");
        if (versionNumber == 1 && previousRevisionId != null)
            throw new DomainInvariantViolationException("version 1 must not have a previousRevisionId");
        if (versionNumber > 1 && previousRevisionId == null)
            throw new DomainInvariantViolationException("versionNumber > 1 must have a previousRevisionId");
        if (origin == RevisionOrigin.LEGACY_UNKNOWN) {
            if (actorId != null) throw new DomainInvariantViolationException("actorId must be null for LEGACY_UNKNOWN revisions");
        } else if (actorId == null || actorId.isBlank()) {
            throw new DomainInvariantViolationException("actorId must not be blank");
        }
        if (createdAt == null) throw new DomainInvariantViolationException("createdAt must not be null");
        AssessmentRevision r = new AssessmentRevision();
        r.id = id;
        r.assessmentId = assessmentId;
        r.versionNumber = versionNumber;
        r.previousRevisionId = previousRevisionId;
        r.origin = origin;
        r.actorId = actorId;
        r.reason = reason;
        r.sourceAgentAttemptId = sourceAgentAttemptId;
        r.title = title;
        r.context = context;
        r.instructions = instructions;
        r.objectives = List.copyOf(objectives);
        r.deliverables = List.copyOf(deliverables);
        r.constraints = List.copyOf(constraints);
        r.createdAt = createdAt;
        return r;
    }

    private static void validateContent(AssessmentId assessmentId, String title, String context, String instructions,
                                         List<String> objectives, List<String> deliverables, List<String> constraints) {
        if (assessmentId == null) throw new DomainInvariantViolationException("assessmentId must not be null");
        if (title == null || title.isBlank())               throw new DomainInvariantViolationException("title must not be blank");
        if (context == null || context.isBlank())            throw new DomainInvariantViolationException("context must not be blank");
        if (instructions == null || instructions.isBlank())  throw new DomainInvariantViolationException("instructions must not be blank");
        if (objectives == null)   throw new DomainInvariantViolationException("objectives must not be null");
        if (deliverables == null) throw new DomainInvariantViolationException("deliverables must not be null");
        if (constraints == null)  throw new DomainInvariantViolationException("constraints must not be null");
    }

    private static void validateActorId(String actorId) {
        if (actorId == null || actorId.isBlank()) throw new DomainInvariantViolationException("actorId must not be blank");
    }

    private static void validateSourceAgentAttemptId(UUID sourceAgentAttemptId) {
        if (sourceAgentAttemptId == null)
            throw new DomainInvariantViolationException("sourceAgentAttemptId must not be null for AI_GENERATED revisions");
    }

    @Override protected UUID id()                  { return id; }
    public UUID getId()                            { return id; }
    public AssessmentId getAssessmentId()          { return assessmentId; }
    public int getVersionNumber()                  { return versionNumber; }
    public UUID getPreviousRevisionId()            { return previousRevisionId; }
    public RevisionOrigin getOrigin()              { return origin; }
    public String getActorId()                     { return actorId; }
    public String getReason()                       { return reason; }
    public UUID getSourceAgentAttemptId()          { return sourceAgentAttemptId; }
    public String getTitle()                        { return title; }
    public String getContext()                      { return context; }
    public String getInstructions()                { return instructions; }
    public List<String> getObjectives()            { return objectives; }
    public List<String> getDeliverables()          { return deliverables; }
    public List<String> getConstraints()           { return constraints; }
    public Instant getCreatedAt()                   { return createdAt; }
}
