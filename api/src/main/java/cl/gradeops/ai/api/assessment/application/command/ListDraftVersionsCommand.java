package cl.gradeops.ai.api.assessment.application.command;

import java.util.UUID;

public record ListDraftVersionsCommand(UUID assessmentId, String teacherUid) {}
