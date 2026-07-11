package cl.gradeops.ai.agents.assessment.application.exception;

import cl.gradeops.ai.agents.assessment.application.result.AgentExecutionLogPayload;

/**
 * The Assessment Agent's own exception type, per {@code
 * 12-excepciones-y-manejo-de-errores.md} — never a Java API exception
 * (`IllegalArgumentException`, `NullPointerException`, ...) is thrown from this feature's
 * application code.
 *
 * <p>{@code agents/} has no domain/application/infrastructure exception hierarchy of its own
 * yet (it has no domain layer at all — {@code CLAUDE.md}: "Agents do not own domain entities").
 * A single concrete exception with a reason code, rather than a subclass per failure, mirrors
 * how {@code api/}'s own {@code DomainInvariantViolationException} is used generically across
 * many different invariant violations. Splitting {@code INVALID_COMMAND}/{@code
 * MALFORMED_OUTPUT} into separate classes now, with only one agent and two reasons, would be
 * abstraction by speculation (`00-principios-rectores.md` #6, KISS) — revisit if a second
 * agent needs its own reason codes.
 *
 * <p>Carries its own (partial, always {@code status="FAILED"}) {@link AgentExecutionLogPayload}
 * so the caller (task-04's exception handler) can still return execution evidence to {@code
 * api/} even when generation never produces an {@code AssessmentExecutionOutcome} —
 * evidence must never be a side effect of the happy path only (`00-principios-rectores.md` #8).
 */
public class AssessmentAgentException extends RuntimeException {

    /** Why the pipeline rejected the request, before or after calling the model. */
    public enum Reason {
        /** {@code AssessmentCommand} failed {@code validate}: blank required field or an
         *  inconsistent regeneration triple. */
        INVALID_COMMAND,
        /** The model's response could not be parsed, or parsed but left a required {@code
         *  AssessmentResult} field blank/empty. */
        MALFORMED_OUTPUT
    }

    private final Reason reason;
    private final AgentExecutionLogPayload log;

    public AssessmentAgentException(Reason reason, String message, AgentExecutionLogPayload log) {
        super(message);
        this.reason = reason;
        this.log = log;
    }

    public Reason reason() {
        return reason;
    }

    public AgentExecutionLogPayload log() {
        return log;
    }
}
