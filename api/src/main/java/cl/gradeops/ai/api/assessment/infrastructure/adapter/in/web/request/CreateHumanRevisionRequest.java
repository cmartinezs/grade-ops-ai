package cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateHumanRevisionRequest(
    @NotNull UUID expectedRevisionId,
    @NotBlank String title,
    @NotBlank String context,
    @NotBlank String instructions,
    @NotEmpty List<String> objectives,
    @NotEmpty List<String> deliverables,
    @NotEmpty List<String> constraints,
    String reason
) {}
