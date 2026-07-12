package cl.gradeops.ai.agents.assessment.infrastructure.adapter.out.groq;

import cl.gradeops.ai.agents.assessment.application.port.out.AssessmentGenerationPort;
import cl.gradeops.ai.agents.assessment.application.port.out.AssessmentGenerationResponse;
import cl.gradeops.ai.agents.assessment.application.result.AssessmentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;

/**
 * Groq's {@code AssessmentGenerationPort} implementation. Groq exposes an OpenAI-compatible
 * chat completions endpoint, so this adapter is built on Spring AI's OpenAI {@code ChatClient}
 * pointed at Groq's base URL — there is no dedicated Groq starter. Structurally mirrors {@code
 * GeminiAssessmentGenerationAdapter}: same one-call {@code responseEntity} usage and the same
 * {@code EmptyUsage} handling (Spring AI's usage metadata is never a literal {@code null}, it
 * defaults to a zero-valued marker type when a provider omits usage data).
 */
@RequiredArgsConstructor
public class GroqAssessmentGenerationAdapter implements AssessmentGenerationPort {

    private final ChatClient chatClient;

    @Override
    public AssessmentGenerationResponse generate(String renderedPrompt) {
        ResponseEntity<ChatResponse, AssessmentResult> responseEntity =
                chatClient.prompt(renderedPrompt).call().responseEntity(AssessmentResult.class);

        ChatResponse chatResponse = responseEntity.response();
        Usage usage = chatResponse.getMetadata() != null ? chatResponse.getMetadata().getUsage() : null;
        boolean usageAvailable = usage != null && !(usage instanceof EmptyUsage);

        return AssessmentGenerationResponse.builder()
                .result(responseEntity.entity())
                .rawResponseText(chatResponse.getResult().getOutput().getText())
                .modelName(chatResponse.getMetadata() != null ? chatResponse.getMetadata().getModel() : null)
                .estimatedInputTokens(usageAvailable ? usage.getPromptTokens() : null)
                .estimatedOutputTokens(usageAvailable ? usage.getCompletionTokens() : null)
                .build();
    }
}
