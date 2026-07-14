package cl.gradeops.ai.api.assessment.application.command;

import java.util.List;
import java.util.UUID;

public record UpdateAssessmentDraftCommand(
    UUID assessmentId,
    String teacherUid,
    String title,
    String context,
    String instructions,
    List<String> objectives,
    List<String> deliverables,
    List<String> constraints
) {}
