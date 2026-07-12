package cl.gradeops.ai.agents.assessment.infrastructure.adapter.out.gemini;

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
 * The only class in this feature allowed to import {@code ChatClient}/{@code ChatResponse},
 * per the project's package conventions for provider-specific adapters. Isolates
 * {@code AssessmentAgentOrchestrator} from Spring AI entirely.
 *
 * <p>Uses {@code ChatClient.CallResponseSpec.responseEntity(Class)} rather than {@code
 * entity(Class)} so both the mapped {@link AssessmentResult} and the raw {@link ChatResponse}
 * (for token-usage metadata) come from a single call — calling {@code entity(...)} and {@code
 * chatResponse()} separately would issue two model calls.
 *
 * <p>{@code ChatResponseMetadata.getUsage()} never actually returns {@code null} — Spring AI
 * defaults it to {@link EmptyUsage}, whose token counts are {@code 0}, not absent. Treating
 * only a literal {@code null} as "no usage data" would silently report {@code 0} tokens
 * whenever a provider doesn't supply usage metadata, instead of the {@code null} this class's
 * contract promises for that case — checking for {@code EmptyUsage} explicitly is what makes
 * "unavailable" distinguishable from "zero".
 */
@RequiredArgsConstructor
public class GeminiAssessmentGenerationAdapter implements AssessmentGenerationPort {

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
