package cl.gradeops.ai.agents.assessment.application.port.out;

import cl.gradeops.ai.agents.assessment.application.result.AssessmentResult;
import lombok.Builder;

/**
 * Raw output of {@link AssessmentGenerationPort#generate(String)} — the mapped structured
 * result plus the model/token metadata {@code AssessmentAgentOrchestrator} needs to build
 * {@code AgentExecutionLogPayload}. This type stays in {@code application.port.out} because it
 * is the port's return shape, not a top-level contract of the feature.
 *
 * @param result the structured assessment mapped from the model's response; never validated
 *     here — {@code AssessmentAgentOrchestrator.validateOutput} owns that
 * @param rawResponseText the model's unparsed response text, used only to compute {@code
 *     AgentExecutionLogPayload.outputHash} — hashing the already-parsed {@code result} would
 *     hash a re-serialization, not what the model actually returned (e.g. it would silently
 *     drop whether the response was markdown-fenced)
 * @param modelName the model that produced the response
 * @param estimatedInputTokens prompt token count if the provider reported it, else {@code null}
 * @param estimatedOutputTokens completion token count if the provider reported it, else {@code
 *     null}
 */
@Builder
public record AssessmentGenerationResponse(
        AssessmentResult result,
        String rawResponseText,
        String modelName,
        Integer estimatedInputTokens,
        Integer estimatedOutputTokens) {
}
