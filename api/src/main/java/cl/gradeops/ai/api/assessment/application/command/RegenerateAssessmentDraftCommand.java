package cl.gradeops.ai.api.assessment.application.command;

import java.util.UUID;

public record RegenerateAssessmentDraftCommand(UUID assessmentId, String teacherUid, String adjustmentNotes) {}
