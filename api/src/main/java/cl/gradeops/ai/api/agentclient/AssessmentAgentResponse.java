package cl.gradeops.ai.api.agentclient;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Field-for-field mirror of {@code agents/}'s {@code AssessmentExecutionResponse}
 * ({@code result} + {@code log}), verified 2026-07-13 against {@code agents/src/main/java/.../
 * assessment/infrastructure/adapter/in/web/response/AssessmentExecutionResponse.java} and the
 * {@code AgentExecutionLogPayload} it wraps. {@code Log} carries the full 13-field payload, not
 * a subset — {@code status} and {@code errorCode} are two separate fields, never conflated.
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
