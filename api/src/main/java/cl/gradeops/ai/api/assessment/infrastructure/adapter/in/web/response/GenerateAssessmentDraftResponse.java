package cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web.response;

import java.util.List;
import java.util.UUID;

/**
 * {@code draftId} is the {@code AssessmentRevision} id — kept its pre-authoring-cut field name
 * for compatibility (see API-A3-HANDOFF.md); this is the value clients send back as {@code
 * expectedRevisionId} on regenerate/create-human-revision. {@code origin}/{@code actorId}/
 * {@code reason}/{@code previousRevisionId} are additive per LOCAL-CONTRACTS.md § API ↔ Web
 * public contract.
 */
public record GenerateAssessmentDraftResponse(
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
) {}
