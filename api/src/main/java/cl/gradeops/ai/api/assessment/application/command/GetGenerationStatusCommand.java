package cl.gradeops.ai.api.assessment.application.command;

import java.util.UUID;

public record GetGenerationStatusCommand(UUID assessmentId, String teacherUid) {}
