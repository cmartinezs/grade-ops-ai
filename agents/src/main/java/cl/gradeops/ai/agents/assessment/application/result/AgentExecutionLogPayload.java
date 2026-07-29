package cl.gradeops.ai.agents.assessment.application.result;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

/**
 * Execution evidence for one Assessment Agent run, returned to {@code api/} for {@code
 * AgentExecutionLog} persistence. Field set matches the project's minimum audit requirements
 * for agent executions, scoped to what this artifact actually knows: no {@code tenantId}/
 * {@code teacherId}/resource id — {@code AssessmentCommand} carries no tenant context, and
 * {@code agents/} never persists anything — {@code api/} attaches those when it persists the
 * final row.
 *
 * <p>{@code status} only ever takes two values here (never {@code "STARTED"}): this is a
 * synchronous call that either completes or throws, so there is no separate "started" state to
 * report — {@code api/} owns any broader lifecycle state its own {@code AgentExecutionLog} row
 * needs.
 *
 * @param agentExecutionId generated fresh per call, for cross-referencing this payload with
 *     whatever {@code api/} logs about the same execution
 * @param agentName constant {@code "assessment"} — this agent's name
 * @param provider the resolved provider name that actually served the request (e.g. {@code
 *     "gemini"}, {@code "groq"}); {@code null} if resolution never happened before failure (e.g.
 *     {@code INVALID_COMMAND}), same nullability discipline as {@code model}
 * @param model the model that produced the response; {@code null} if the call never reached the
 *     provider (e.g. {@code INVALID_COMMAND})
 * @param promptVersion the rendered template's header-comment version (task-02)
 * @param inputHash SHA-256 hex of the rendered prompt — the raw prompt itself is never logged
 * @param outputHash SHA-256 hex of the raw model response text; {@code null} if generation
 *     failed before a response existed
 * @param estimatedInputTokens {@code null} if usage metadata was unavailable or the call never
 *     reached the provider
 * @param estimatedOutputTokens {@code null} under the same conditions as {@code
 *     estimatedInputTokens}
 * @param costEstimate best-effort estimate from token counts; {@code null} if tokens are
 *     unavailable
 * @param status {@code "COMPLETED"} or {@code "FAILED"}
 * @param errorCode {@link cl.gradeops.ai.agents.assessment.application.exception.AssessmentAgentException.Reason}
 *     name when {@code status="FAILED"}, else {@code null}
 * @param startedAt when this pipeline run began
 * @param finishedAt when this pipeline run ended (success or failure)
 */
@Builder
public record AgentExecutionLogPayload(
        UUID agentExecutionId,
        String agentName,
        String provider,
        String model,
        String promptVersion,
        String inputHash,
        String outputHash,
        Integer estimatedInputTokens,
        Integer estimatedOutputTokens,
        Double costEstimate,
        String status,
        String errorCode,
        Instant startedAt,
        Instant finishedAt) {
}
