package cl.gradeops.ai.api.assessment.application.usecase;

import cl.gradeops.ai.api.agentclient.AgentClientException;
import cl.gradeops.ai.api.assessment.application.exception.StaleOnCompletionException;

/**
 * Shared mapping between a durable {@code AgentAttempt.failureCode} and (a) the HTTP status a
 * failed regenerate originally produced, recorded on the idempotency record, and (b) the
 * exception to re-throw on replay so {@code GlobalExceptionHandler} reproduces that same
 * status/body — a replay of a failed regenerate must never surface as a different, generic error
 * (A3 Final Idempotency Correction § 3.4). Regenerate never returns {@code 202}; unlike initial
 * generation, its failures propagate as the same typed exceptions {@code agents/} dispatch itself
 * would have thrown, so a replay reconstructs one of those exceptions rather than a status code.
 */
final class RegenerateFailureReplay {

    private RegenerateFailureReplay() {}

    static int httpStatus(String failureCode) {
        return switch (failureCode) {
            case "AGENT_UNAVAILABLE" -> 503;
            case "AGENT_ERROR" -> 502;
            case "STALE_ON_COMPLETION" -> 409;
            default -> 422; // AGENT_REJECTED, INVALID_COMMAND, MALFORMED_OUTPUT
        };
    }

    static RuntimeException reconstruct(String assessmentId, String failureCode) {
        if ("STALE_ON_COMPLETION".equals(failureCode)) {
            return new StaleOnCompletionException(assessmentId);
        }
        AgentClientException.Reason reason = switch (failureCode) {
            case "AGENT_UNAVAILABLE" -> AgentClientException.Reason.UNREACHABLE;
            case "AGENT_ERROR" -> AgentClientException.Reason.AGENT_ERROR;
            default -> AgentClientException.Reason.AGENT_REJECTED; // AGENT_REJECTED, INVALID_COMMAND, MALFORMED_OUTPUT
        };
        return new AgentClientException(reason,
                "Replayed idempotent regenerate failure (original failureCode=" + failureCode + ")", null);
    }
}
