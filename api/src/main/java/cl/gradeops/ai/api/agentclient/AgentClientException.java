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

    public AgentClientException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }
}
