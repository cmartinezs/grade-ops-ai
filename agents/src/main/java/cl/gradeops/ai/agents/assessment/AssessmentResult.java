package cl.gradeops.ai.agents.assessment;

import static java.util.Objects.requireNonNull;

import java.util.List;

/**
 * Structured output of the Assessment Agent (US-010/US-011/US-012).
 *
 * <p>Returned for both initial draft generation (US-011) and regeneration (US-012).
 * The agent never persists this data; persistence is {@code api/}'s responsibility.
 *
 * @param title assessment title
 * @param context scenario or framing given to the student
 * @param instructions student-facing instructions
 * @param objectives learning objectives the assessment evaluates
 * @param deliverables expected student deliverables
 * @param constraints rules, resources, or submission constraints
 */
public record AssessmentResult(
        String title,
        String context,
        String instructions,
        List<String> objectives,
        List<String> deliverables,
        List<String> constraints) {

    public AssessmentResult {
        requireNonNull(title, "title is required");
        requireNonNull(context, "context is required");
        requireNonNull(instructions, "instructions is required");
        objectives = List.copyOf(requireNonNull(objectives, "objectives is required"));
        deliverables = List.copyOf(requireNonNull(deliverables, "deliverables is required"));
        constraints = List.copyOf(requireNonNull(constraints, "constraints is required"));
    }
}
