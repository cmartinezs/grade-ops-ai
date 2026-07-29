package cl.gradeops.ai.api.agentclient;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Field-for-field mirror of {@code agents/}'s {@code AssessmentExecutionResponse}
 * ({@code result} + {@code log}), re-verified 2026-07-29 (Task 07A/07C) against Agents'
 * {@code AgentExecutionLogPayload} — now 14 fields: {@code provider} was added, additive,
 * between {@code agentName} and {@code model}. {@code Log} carries the full payload, not a
 * subset — {@code status} and {@code errorCode} are two separate fields, never conflated.
 *
 * <p>{@code provider} nullability mirrors {@code model}'s: non-null on a successful generation
 * or on a failure that occurred after provider resolution (e.g. {@code MALFORMED_OUTPUT}); null
 * when the pipeline rejected the command before resolution ever ran ({@code INVALID_COMMAND}).
 */
public record AssessmentAgentResponse(Result result, Log log) {

    public record Result(
            String title,
            String context,
            String instructions,
            List<String> objectives,
            List<String> deliverables,
            List<String> constraints) {
    }

    public record Log(
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
}
