package cl.gradeops.ai.api.assessment.application.command;

import java.util.UUID;

public record GenerateAssessmentDraftCommand(UUID assessmentId, String teacherUid) {}
