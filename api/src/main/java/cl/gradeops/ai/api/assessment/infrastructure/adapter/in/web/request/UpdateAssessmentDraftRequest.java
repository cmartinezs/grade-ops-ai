package cl.gradeops.ai.api.assessment.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * All fields optional — only the fields present in a given request are updated (US-013).
 * Per Bean Validation's own convention, {@code null} is always valid regardless of the
 * constraint below, so an absent field means "don't change this field"; {@code @Size(min = 1)}
 * only rejects an explicitly-sent empty string, and {@code @NotBlank} on the list element type
 * rejects a blank entry within a provided list.
 */
public record UpdateAssessmentDraftRequest(
    @Size(min = 1, message = "must not be blank if provided") String title,
    @Size(min = 1, message = "must not be blank if provided") String context,
    @Size(min = 1, message = "must not be blank if provided") String instructions,
    List<@NotBlank String> objectives,
    List<@NotBlank String> deliverables,
    List<@NotBlank String> constraints
) {}
