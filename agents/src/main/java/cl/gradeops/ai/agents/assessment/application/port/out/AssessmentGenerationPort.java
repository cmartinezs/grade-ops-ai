package cl.gradeops.ai.agents.assessment.application.port.out;

/**
 * Abstracts "generate a structured assessment from a rendered prompt" away from the concrete AI
 * provider. {@code AssessmentAgentOrchestrator} depends only on this port — Spring AI's {@code
 * ChatClient}/{@code ChatResponse} types are never imported outside the implementing adapter
 * ({@code GeminiAssessmentGenerationAdapter}, {@code infrastructure.adapter.out.gemini}).
 */
public interface AssessmentGenerationPort {

    /**
     * @param renderedPrompt the fully-rendered {@code assessment-generation.st} prompt text
     * @return the mapped result plus model/token metadata
     * @throws RuntimeException if the provider's response cannot be parsed into an {@code
     *     AssessmentResult} at all (not merely incomplete — that is {@code
     *     AssessmentAgentOrchestrator.validateOutput}'s concern). Implementations let such
     *     failures propagate; the orchestrator translates them into {@code
     *     AssessmentAgentException(MALFORMED_OUTPUT)}.
     */
    AssessmentGenerationResponse generate(String renderedPrompt);
}
