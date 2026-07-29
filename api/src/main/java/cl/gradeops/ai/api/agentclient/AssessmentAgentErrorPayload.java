package cl.gradeops.ai.api.agentclient;

/**
 * Field-for-field mirror of {@code agents/}'s {@code AgentErrorResponse} — the JSON body
 * {@code agents/} returns on a 4xx (422) {@code AssessmentAgentException}. {@code errorCode} is
 * the agents-side detailed reason ({@code INVALID_COMMAND}/{@code MALFORMED_OUTPUT}) that
 * {@code AgentClientException.Reason.AGENT_REJECTED} alone collapses away — parsed here so the
 * durable coordinator can persist the verbatim code onto {@code AgentAttempt.failureCode}
 * instead of only the coarser transport-level reason.
 */
public record AssessmentAgentErrorPayload(
        String errorCode, String message, AssessmentAgentResponse.Log log, String correlationId) {
}
