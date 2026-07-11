package cl.gradeops.ai.agents.assessment.infrastructure.config;

import cl.gradeops.ai.agents.assessment.application.orchestrator.AssessmentAgentOrchestrator;
import cl.gradeops.ai.agents.assessment.application.port.out.AssessmentGenerationPort;
import cl.gradeops.ai.agents.assessment.application.usecase.GenerateAssessmentDraftHandler;
import cl.gradeops.ai.agents.assessment.infrastructure.adapter.out.gemini.GeminiAssessmentGenerationAdapter;
import org.springframework.ai.chat.client.ChatClient;
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
 */
@Configuration
@ConditionalOnProperty(prefix = "app.agents.gemini", name = "enabled", havingValue = "true", matchIfMissing = true)
class AssessmentConfig {

    @Bean
    GeminiAssessmentGenerationAdapter geminiAssessmentGenerationAdapter(ChatClient.Builder chatClientBuilder) {
        return new GeminiAssessmentGenerationAdapter(chatClientBuilder.build());
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
