package cl.gradeops.ai.api.assessment.application.result;

import java.util.List;
import java.util.UUID;

/**
 * {@code draftId} is the {@code AssessmentRevision} id — the field kept its pre-authoring-cut
 * name for API-response compatibility (see API-A3-HANDOFF.md); it is not a separate identifier
 * from the revision id. {@code origin}/{@code actorId}/{@code reason}/{@code previousRevisionId}
 * are additive per LOCAL-CONTRACTS.md § API ↔ Web public contract.
 */
public record GenerateAssessmentDraftResult(
    UUID draftId,
    String title,
    String context,
    String instructions,
    List<String> objectives,
    List<String> deliverables,
    List<String> constraints,
    int versionNumber,
    String origin,
    String actorId,
    String reason,
    UUID previousRevisionId
) {
    public GenerateAssessmentDraftResult(UUID draftId, String title, String context, String instructions,
            List<String> objectives, List<String> deliverables, List<String> constraints, int versionNumber) {
        this(draftId, title, context, instructions, objectives, deliverables, constraints, versionNumber,
                null, null, null, null);
    }

    public static GenerateAssessmentDraftResult fromRevision(cl.gradeops.ai.api.assessment.domain.model.AssessmentRevision revision) {
        return new GenerateAssessmentDraftResult(revision.getId(), revision.getTitle(), revision.getContext(),
                revision.getInstructions(), revision.getObjectives(), revision.getDeliverables(),
                revision.getConstraints(), revision.getVersionNumber(), revision.getOrigin().name(),
                revision.getActorId(), revision.getReason(), revision.getPreviousRevisionId());
    }
}
