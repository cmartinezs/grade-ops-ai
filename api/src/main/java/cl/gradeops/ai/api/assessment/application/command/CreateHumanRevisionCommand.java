package cl.gradeops.ai.api.assessment.application.command;

import java.util.List;
import java.util.UUID;

public record CreateHumanRevisionCommand(
    UUID assessmentId,
    String teacherUid,
    UUID expectedRevisionId,
    String title,
    String context,
    String instructions,
    List<String> objectives,
    List<String> deliverables,
    List<String> constraints,
    String reason
) {}
