package cl.gradeops.ai.api.assessment.application.command;

public record CreateAssessmentBriefCommand(
    String teacherUid,
    String learningGoal,
    String topic,
    String level,
    String duration,
    String language
) {}
