package cl.gradeops.ai.api.assessment.application.result;

import java.util.List;
import java.util.UUID;

public record GenerateAssessmentDraftResult(
    UUID draftId,
    String title,
    String context,
    String instructions,
    List<String> objectives,
    List<String> deliverables,
    List<String> constraints,
    int versionNumber
) {}
