package cl.gradeops.ai.api.agentclient;

/**
 * Wraps every failure mode of an {@code agentclient} call so none of them propagate to a caller
 * as a raw connection or HTTP exception.
 */
public class AgentClientException extends RuntimeException {

    /** Why the call to {@code agents/} failed. */
    public enum Reason {
        /** No response was received: connection refused, DNS failure, or timeout. */
        UNREACHABLE,
        /** {@code agents/} rejected the request (4xx) — e.g. an invalid or inconsistent command. */
        AGENT_REJECTED,
        /** {@code agents/} failed while processing the request (5xx). */
        AGENT_ERROR
    }

    private final Reason reason;
    private final AssessmentAgentErrorPayload agentError;

    public AgentClientException(Reason reason, String message, Throwable cause) {
        this(reason, message, cause, null);
    }

    public AgentClientException(Reason reason, String message, Throwable cause, AssessmentAgentErrorPayload agentError) {
        super(message, cause);
        this.reason = reason;
        this.agentError = agentError;
    }

    public Reason reason() {
        return reason;
    }

    /** Non-null only when the 4xx response body was parseable as {@code agents/}'s error shape. */
    public AssessmentAgentErrorPayload agentError() {
        return agentError;
    }
}
