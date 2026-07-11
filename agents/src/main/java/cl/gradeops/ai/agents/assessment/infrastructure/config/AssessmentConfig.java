package cl.gradeops.ai.agents.assessment.infrastructure.config;

import cl.gradeops.ai.agents.assessment.application.orchestrator.AssessmentAgentOrchestrator;
import cl.gradeops.ai.agents.assessment.application.port.out.AssessmentGenerationPort;
import cl.gradeops.ai.agents.assessment.application.usecase.GenerateAssessmentDraftHandler;
import cl.gradeops.ai.agents.assessment.infrastructure.adapter.out.gemini.GeminiAssessmentGenerationAdapter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers this feature's application/infrastructure beans explicitly, per the project's
 * wiring conventions — none of the classes wired here carry {@code @Service}/{@code
 * @Component}.
 *
 * <p>Gated on {@code ChatClient.Builder} being present: the {@code test} Spring profile
 * (`src/test/resources/application-test.yml`) intentionally excludes Google GenAI's
 * autoconfiguration to keep {@code GradeOpsAgentsApplicationTest#contextLoads} hermetic, so no
 * such bean exists there. Without this guard, the context fails to start under {@code test} —
 * `beta`/`demo` (where the real autoconfiguration is active) are unaffected.
 */
@Configuration
@ConditionalOnBean(ChatClient.Builder.class)
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
