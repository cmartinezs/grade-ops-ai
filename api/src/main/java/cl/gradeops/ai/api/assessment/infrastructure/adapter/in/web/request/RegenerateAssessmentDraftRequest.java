package cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;

public record RegenerateAssessmentDraftRequest(@NotBlank String adjustmentNotes) {}
