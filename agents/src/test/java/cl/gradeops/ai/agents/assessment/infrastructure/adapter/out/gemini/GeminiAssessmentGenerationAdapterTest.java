package cl.gradeops.ai.agents.assessment.infrastructure.adapter.out.gemini;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import cl.gradeops.ai.agents.assessment.application.port.out.AssessmentGenerationResponse;
import cl.gradeops.ai.agents.assessment.application.result.AssessmentResult;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

@ExtendWith(MockitoExtension.class)
class GeminiAssessmentGenerationAdapterTest {

    private static final String RENDERED_PROMPT = "Generate an assessment for this brief...";

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @Mock
    private Usage usage;

    private GeminiAssessmentGenerationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new GeminiAssessmentGenerationAdapter(chatClient);
    }

    private static AssessmentResult completeResult() {
        return AssessmentResult.builder()
                .title("Loop exercise")
                .context("Practice iteration")
                .instructions("Implement the requested program")
                .objectives(List.of("Use for loops"))
                .deliverables(List.of("Source code"))
                .constraints(List.of("No external libraries"))
                .build();
    }

    @Test
    void shouldMapResultAndTokenUsageWhenMetadataIsPresent() {
        // given
        AssessmentResult expectedResult = completeResult();
        when(usage.getPromptTokens()).thenReturn(120);
        when(usage.getCompletionTokens()).thenReturn(80);
        ChatResponseMetadata metadata =
                ChatResponseMetadata.builder().model("gemini-2.0-flash").usage(usage).build();
        ChatResponse chatResponse = new ChatResponse(
                List.of(new Generation(new AssistantMessage("{\"title\":\"Loop exercise\"}"))), metadata);
        ResponseEntity<ChatResponse, AssessmentResult> responseEntity =
                new ResponseEntity<>(chatResponse, expectedResult);

        when(chatClient.prompt(RENDERED_PROMPT)).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.responseEntity(AssessmentResult.class)).thenReturn(responseEntity);

        // when
        AssessmentGenerationResponse response = adapter.generate(RENDERED_PROMPT);

        // then — 1. no nulo
        assertThat(response).isNotNull();
        // then — 2. atributos no nulos
        assertThat(response.result()).isNotNull();
        assertThat(response.rawResponseText()).isNotNull();
        assertThat(response.modelName()).isNotNull();
        assertThat(response.estimatedInputTokens()).isNotNull();
        assertThat(response.estimatedOutputTokens()).isNotNull();
        // then — 3. valores esperados
        assertThat(response.result()).isEqualTo(expectedResult);
        assertThat(response.rawResponseText()).isEqualTo("{\"title\":\"Loop exercise\"}");
        assertThat(response.modelName()).isEqualTo("gemini-2.0-flash");
        assertThat(response.estimatedInputTokens()).isEqualTo(120);
        assertThat(response.estimatedOutputTokens()).isEqualTo(80);
    }

    @Test
    void shouldReturnNullTokenFieldsWhenUsageMetadataIsUnavailable() {
        // given
        AssessmentResult expectedResult = completeResult();
        ChatResponseMetadata metadata = ChatResponseMetadata.builder().model("gemini-2.0-flash").build();
        ChatResponse chatResponse = new ChatResponse(
                List.of(new Generation(new AssistantMessage("{\"title\":\"Loop exercise\"}"))), metadata);
        ResponseEntity<ChatResponse, AssessmentResult> responseEntity =
                new ResponseEntity<>(chatResponse, expectedResult);

        when(chatClient.prompt(RENDERED_PROMPT)).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.responseEntity(AssessmentResult.class)).thenReturn(responseEntity);

        // when
        AssessmentGenerationResponse response = adapter.generate(RENDERED_PROMPT);

        // then — 1. no nulo
        assertThat(response).isNotNull();
        // then — 2. atributos no nulos (los que sí deben venir poblados)
        assertThat(response.result()).isNotNull();
        assertThat(response.modelName()).isNotNull();
        // then — 3. valores esperados, incluidos los nulos esperados por falta de metadata
        assertThat(response.result()).isEqualTo(expectedResult);
        assertThat(response.modelName()).isEqualTo("gemini-2.0-flash");
        assertThat(response.estimatedInputTokens()).isNull();
        assertThat(response.estimatedOutputTokens()).isNull();
    }
}
