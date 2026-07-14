package cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record CreateAssessmentBriefRequest(
    @NotBlank String learningGoal,
    @NotBlank String topic,
    @NotBlank String level,
    @NotBlank String duration,
    @NotBlank String language
) {}
