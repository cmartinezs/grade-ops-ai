package cl.gradeops.ai.agents.assessment.application.result;

import lombok.Builder;

/**
 * What {@code GenerateAssessmentDraftUseCase.execute} returns on success: the structured draft
 * plus the execution evidence for the same call. Bundled together because {@code api/} always
 * needs both — the draft to show the teacher, the log to persist as {@code AgentExecutionLog}.
 *
 * @param result the structured assessment draft
 * @param log execution evidence for this call ({@code status="COMPLETED"})
 */
@Builder
public record AssessmentExecutionOutcome(AssessmentResult result, AgentExecutionLogPayload log) {
}
