package cl.gradeops.ai.agents.shared.infrastructure.adapter.in.web.response;

import cl.gradeops.ai.agents.assessment.application.result.AgentExecutionLogPayload;

/**
 * JSON error body for any {@code agents/} internal endpoint, not assessment-specific. Always
 * carries {@code log} — even a rejected/failed request produces execution evidence, not just
 * an error message, so {@code api/} can persist it the same way a successful call's evidence
 * is persisted.
 *
 * <p>Referencing {@code AgentExecutionLogPayload} here ties this otherwise feature-agnostic
 * type to the assessment feature, which is only correct while assessment is the only agent.
 * Genericizing the log shape now, with nothing to generalize from yet, would be guessing at a
 * second agent's needs — revisit when one actually exists.
 */
public record AgentErrorResponse(
        String errorCode, String message, AgentExecutionLogPayload log, String correlationId) {
}
