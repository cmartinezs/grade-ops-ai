package cl.gradeops.ai.agents.assessment.application.result;

import java.util.List;
import lombok.Builder;

/**
 * Structured output of the Assessment Agent (US-010/US-011/US-012).
 *
 * <p>Returned for both initial draft generation (US-011) and regeneration (US-012).
 * The agent never persists this data; persistence is {@code api/}'s responsibility.
 *
 * <p>The compact constructor only guarantees immutability (defensive copy of the list
 * fields, normalizing {@code null} to an empty list). It never rejects missing required
 * fields: this record is the target of Spring AI's structured-output deserialization from
 * Gemini's response, so an absent field is expected model output, not a caller bug. Required-
 * field validation belongs to {@code AssessmentAgentService.validateOutput}, which rejects
 * incomplete results with {@code AssessmentAgentException} per the project's own exception
 * hierarchy (see {@code 12-excepciones-y-manejo-de-errores.md}) rather than a Java API
 * exception thrown from this constructor.
 *
 * @param title assessment title
 * @param context scenario or framing given to the student
 * @param instructions student-facing instructions
 * @param objectives learning objectives the assessment evaluates
 * @param deliverables expected student deliverables
 * @param constraints rules, resources, or submission constraints
 */
@Builder
public record AssessmentResult(
        String title,
        String context,
        String instructions,
        List<String> objectives,
        List<String> deliverables,
        List<String> constraints) {

    public AssessmentResult {
        objectives = objectives == null ? List.of() : List.copyOf(objectives);
        deliverables = deliverables == null ? List.of() : List.copyOf(deliverables);
        constraints = constraints == null ? List.of() : List.copyOf(constraints);
    }
}
