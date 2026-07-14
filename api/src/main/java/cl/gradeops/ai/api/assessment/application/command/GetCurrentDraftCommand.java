package cl.gradeops.ai.api.assessment.application.command;

import java.util.UUID;

public record GetCurrentDraftCommand(UUID assessmentId, String teacherUid) {}
