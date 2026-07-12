package cl.gradeops.ai.agents.assessment.infrastructure.adapter.out.groq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import cl.gradeops.ai.agents.assessment.application.result.AssessmentResult;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.model.chat.client.autoconfigure.ChatClientAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration;
import org.springframework.ai.model.tool.autoconfigure.ToolCallingAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * On-demand diagnostic — exercises the real {@code ChatClient.responseEntity(AssessmentResult.class)}
 * path against Groq's live API via Spring AI's actual {@code OpenAiChatAutoConfiguration}, the
 * exact mechanism {@code GroqAssessmentGenerationAdapter} uses. Not part of the automated suite
 * task-04 owns — the filename intentionally does not match Surefire's default test-include
 * patterns (`**Test.java`/`Test*.java`/`**Tests.java`), so `./mvnw test` never picks it up; run
 * it explicitly with {@code -Dtest=GroqChatClientManualVerification} whenever Groq's behavior,
 * a chosen model, or this codebase's Spring AI version needs re-checking — e.g. after a Groq
 * model deprecation, a Spring AI upgrade, or before promoting a different default model.
 *
 * <p>Requires {@code GROQ_API_KEY} (a real key) and optionally {@code GROQ_MODEL} (defaults to
 * {@code llama-3.3-70b-versatile} — chosen because {@code llama-3.1-8b-instant} was verified to
 * fail this exact path: it echoes the JSON Schema back instead of filling it in, see
 * {@code task-01-groq-structured-output-evidence.md}). Skips (does not fail) when no key is set,
 * so an accidental {@code -Dtest=GroqChatClientManualVerification} run without credentials
 * doesn't look like a real failure.
 */
class GroqChatClientManualVerification {

    private static final String GROQ_BASE_URL = "https://api.groq.com/openai/v1";
    private static final String DEFAULT_MODEL = "llama-3.3-70b-versatile";

    @Test
    void realChatClientCallAgainstGroq() {
        String apiKey = System.getenv("GROQ_API_KEY");
        assumeTrue(apiKey != null && !apiKey.isBlank(), "GROQ_API_KEY not set — skipping manual diagnostic");
        String model = System.getenv().getOrDefault("GROQ_MODEL", DEFAULT_MODEL);

        String renderedPrompt =
                "Generate an assessment for: evaluate loops and conditionals in Java for "
                        + "first-semester students, introductory level, 60 minutes.";

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        ToolCallingAutoConfiguration.class,
                        OpenAiChatAutoConfiguration.class,
                        ChatClientAutoConfiguration.class))
                .withPropertyValues(
                        "spring.ai.openai.api-key=" + apiKey,
                        "spring.ai.openai.base-url=" + GROQ_BASE_URL,
                        "spring.ai.openai.chat.options.model=" + model)
                .run(context -> {
                    ChatClient chatClient =
                            context.getBean(ChatClient.Builder.class).build();

                    ResponseEntity<ChatResponse, AssessmentResult> responseEntity =
                            chatClient.prompt(renderedPrompt).call().responseEntity(AssessmentResult.class);

                    System.out.println("=== MODEL === " + model);
                    System.out.println("=== ENTITY ===");
                    System.out.println(responseEntity.entity());
                    System.out.println("=== RAW TEXT ===");
                    System.out.println(responseEntity.response().getResult().getOutput().getText());
                    System.out.println("=== USAGE ===");
                    System.out.println(responseEntity.response().getMetadata().getUsage());

                    AssessmentResult result = responseEntity.entity();
                    assertThat(result).isNotNull();
                    assertThat(result.title())
                            .as("title — a schema echo instead of data leaves this null")
                            .isNotBlank();
                    assertThat(result.objectives()).as("objectives").isNotEmpty();
                });
    }
}
