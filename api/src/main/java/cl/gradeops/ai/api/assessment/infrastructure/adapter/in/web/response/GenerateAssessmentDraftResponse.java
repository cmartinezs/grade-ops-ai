package cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web.response;

import java.util.List;
import java.util.UUID;

public record GenerateAssessmentDraftResponse(
    UUID draftId,
    String title,
    String context,
    String instructions,
    List<String> objectives,
    List<String> deliverables,
    List<String> constraints,
    int versionNumber
) {}
