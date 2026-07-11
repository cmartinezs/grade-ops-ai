package cl.gradeops.ai.agents.assessment.infrastructure.adapter.in.web.response;

import cl.gradeops.ai.agents.assessment.application.result.AgentExecutionLogPayload;
import cl.gradeops.ai.agents.assessment.application.result.AssessmentExecutionOutcome;
import cl.gradeops.ai.agents.assessment.application.result.AssessmentResult;

/**
 * JSON response body for {@code POST /internal/agents/assessment} — the transport-level
 * wrapper around {@link AssessmentExecutionOutcome}, kept separate so the application-layer
 * record never needs a Jackson annotation.
 */
public record AssessmentExecutionResponse(AssessmentResult result, AgentExecutionLogPayload log) {

    public static AssessmentExecutionResponse from(AssessmentExecutionOutcome outcome) {
        return new AssessmentExecutionResponse(outcome.result(), outcome.log());
    }
}
