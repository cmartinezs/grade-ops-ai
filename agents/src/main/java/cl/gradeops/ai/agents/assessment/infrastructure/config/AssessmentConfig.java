package cl.gradeops.ai.agents.assessment.infrastructure.config;

import cl.gradeops.ai.agents.assessment.application.orchestrator.AssessmentAgentOrchestrator;
import cl.gradeops.ai.agents.assessment.application.port.out.AssessmentGenerationPort;
import cl.gradeops.ai.agents.assessment.application.usecase.GenerateAssessmentDraftHandler;
import cl.gradeops.ai.agents.assessment.infrastructure.adapter.out.gemini.GeminiAssessmentGenerationAdapter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
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
 * <p>Builds Gemini's {@code ChatClient} directly from the {@code googleGenAiChatModel} bean
 * (Spring AI's own unique name for it) rather than injecting the generic, autoconfigured {@code
 * ChatClient.Builder} — with Groq's OpenAI-compatible starter also on the classpath (added for
 * this planning), Spring AI's {@code ChatClientAutoConfiguration} finds two {@code ChatModel}
 * beans and refuses to build that generic bean at all. `application-beta.yml`/`application-
 * demo.yml` exclude that autoconfiguration entirely for this reason.
 */
@Configuration
@ConditionalOnProperty(prefix = "app.agents.gemini", name = "enabled", havingValue = "true", matchIfMissing = true)
class AssessmentConfig {

    @Bean
    GeminiAssessmentGenerationAdapter geminiAssessmentGenerationAdapter(
            @Qualifier("googleGenAiChatModel") ChatModel chatModel) {
        return new GeminiAssessmentGenerationAdapter(ChatClient.builder(chatModel).build());
    }

    @Bean
    AssessmentAgentOrchestrator assessmentAgentOrchestrator(AssessmentGenerationPort assessmentGenerationPort) {
        return new AssessmentAgentOrchestrator(assessmentGenerationPort);
    }

    @Bean
    GenerateAssessmentDraftHandler generateAssessmentDraftHandler(AssessmentAgentOrchestrator orchestrator) {
        return new GenerateAssessmentDraftHandler(orchestrator);
    }
}
