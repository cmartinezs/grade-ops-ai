package cl.gradeops.ai.api.assessment.application.command;

import java.util.UUID;

public record RetryGenerationCommand(UUID assessmentId, String teacherUid) {}
