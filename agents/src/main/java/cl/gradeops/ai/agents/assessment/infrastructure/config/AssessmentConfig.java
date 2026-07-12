package cl.gradeops.ai.agents.assessment.infrastructure.config;

import cl.gradeops.ai.agents.assessment.application.orchestrator.AssessmentAgentOrchestrator;
import cl.gradeops.ai.agents.assessment.application.port.out.AssessmentGenerationPort;
import cl.gradeops.ai.agents.assessment.application.port.out.AssessmentGenerationPortSelector;
import cl.gradeops.ai.agents.assessment.application.usecase.GenerateAssessmentDraftHandler;
import cl.gradeops.ai.agents.assessment.infrastructure.adapter.out.gemini.GeminiAssessmentGenerationAdapter;
import cl.gradeops.ai.agents.assessment.infrastructure.adapter.out.groq.GroqAssessmentGenerationAdapter;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers this feature's application/infrastructure beans explicitly, per the project's
 * wiring conventions — none of the classes wired here carry {@code @Service}/{@code
 * @Component}.
 *
 * <p>Gated on {@code app.agents.gemini.enabled} (default {@code true}) rather than on {@code
 * ChatClient.Builder} being present: a {@code @ConditionalOnBean} check here would be evaluated
 * before Spring AI's own autoconfiguration has had a chance to register that bean, since this
 * is a regular component-scanned {@code @Configuration}, not an auto-configuration class with
 * defined ordering relative to Spring AI's — it would incorrectly conclude "absent" even when
 * the bean will exist. A property condition has no such ordering dependency. The {@code test}
 * Spring profile (`src/test/resources/application-test.yml`) sets this property {@code false}
 * (alongside excluding Google GenAI's autoconfiguration entirely) to keep {@code
 * GradeOpsAgentsApplicationTest#contextLoads} hermetic; `beta`/`demo` need no change since the
 * default is {@code true}.
 *
 * <p>Builds each provider's {@code ChatClient} directly from its own uniquely named {@code
 * ChatModel} bean ({@code googleGenAiChatModel}, {@code openAiChatModel} — Spring AI's own names)
 * rather than injecting the generic, autoconfigured {@code ChatClient.Builder} — with both
 * Google GenAI's and Groq's OpenAI-compatible starters on the classpath, Spring AI's {@code
 * ChatClientAutoConfiguration} finds two {@code ChatModel} beans and refuses to build that
 * generic bean at all. `application-beta.yml`/`application-demo.yml` exclude that
 * autoconfiguration entirely for this reason.
 *
 * <p>Both adapters are registered as named beans ({@code "gemini"}, {@code "groq"}) so Spring's
 * {@code Map<String, AssessmentGenerationPort>} autowiring can collect them into the {@link
 * AssessmentGenerationPortSelector}'s Strategy-pattern map, keyed by bean name. A single
 * property still gates this whole configuration class (not one flag per provider) — the {@code
 * test} Spring profile needs the entire assessment feature off, since it has no real {@code
 * ChatModel} beans for either provider; there is no scenario yet where one provider must be
 * enabled while the other is disabled, so a second, per-provider flag would be speculative.
 */
@Configuration
@ConditionalOnProperty(prefix = "app.agents.gemini", name = "enabled", havingValue = "true", matchIfMissing = true)
class AssessmentConfig {

    @Value("${app.agents.llm.default-provider}")
    private String defaultProvider;

    @Value("${app.agents.llm.cost-per-1k-tokens.gemini}")
    private double geminiCostPerKTokens;

    @Value("${app.agents.llm.cost-per-1k-tokens.groq}")
    private double groqCostPerKTokens;

    @Bean(name = "gemini")
    GeminiAssessmentGenerationAdapter geminiAssessmentGenerationAdapter(
            @Qualifier("googleGenAiChatModel") ChatModel chatModel) {
        return new GeminiAssessmentGenerationAdapter(ChatClient.builder(chatModel).build());
    }

    @Bean(name = "groq")
    GroqAssessmentGenerationAdapter groqAssessmentGenerationAdapter(
            @Qualifier("openAiChatModel") ChatModel chatModel) {
        return new GroqAssessmentGenerationAdapter(ChatClient.builder(chatModel).build());
    }

    @Bean
    AssessmentGenerationPortSelector assessmentGenerationPortSelector(
            Map<String, AssessmentGenerationPort> portsByProvider) {
        return new AssessmentGenerationPortSelector(portsByProvider, defaultProvider);
    }

    @Bean
    AssessmentAgentOrchestrator assessmentAgentOrchestrator(AssessmentGenerationPortSelector selector) {
        return new AssessmentAgentOrchestrator(
                selector, Map.of("gemini", geminiCostPerKTokens, "groq", groqCostPerKTokens));
    }

    @Bean
    GenerateAssessmentDraftHandler generateAssessmentDraftHandler(AssessmentAgentOrchestrator orchestrator) {
        return new GenerateAssessmentDraftHandler(orchestrator);
    }
}
