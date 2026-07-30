package cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RegenerateAssessmentDraftRequest(
    @NotBlank String adjustmentNotes,
    @NotNull UUID expectedRevisionId
) {}
