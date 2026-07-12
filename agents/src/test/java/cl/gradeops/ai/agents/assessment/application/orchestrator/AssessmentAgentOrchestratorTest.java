package cl.gradeops.ai.agents.assessment.application.orchestrator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cl.gradeops.ai.agents.assessment.application.command.AssessmentCommand;
import cl.gradeops.ai.agents.assessment.application.exception.AssessmentAgentException;
import cl.gradeops.ai.agents.assessment.application.exception.AssessmentAgentException.Reason;
import cl.gradeops.ai.agents.assessment.application.port.out.AssessmentGenerationPort;
import cl.gradeops.ai.agents.assessment.application.port.out.AssessmentGenerationPortSelector;
import cl.gradeops.ai.agents.assessment.application.port.out.AssessmentGenerationResponse;
import cl.gradeops.ai.agents.assessment.application.result.AgentExecutionLogPayload;
import cl.gradeops.ai.agents.assessment.application.result.AssessmentExecutionOutcome;
import cl.gradeops.ai.agents.assessment.application.result.AssessmentResult;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AssessmentAgentOrchestratorTest {

    private static final String PROVIDER = "gemini";
    private static final double COST_PER_1K_TOKENS = 0.000075;

    @Mock
    private AssessmentGenerationPort assessmentGenerationPort;

    private AssessmentAgentOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        // The selector is a real Strategy-pattern resolver wrapping the mocked port under the
        // key these tests exercise — only the port itself is a mock, not the resolution logic.
        AssessmentGenerationPortSelector selector =
                new AssessmentGenerationPortSelector(Map.of(PROVIDER, assessmentGenerationPort), PROVIDER);
        orchestrator = new AssessmentAgentOrchestrator(selector, Map.of(PROVIDER, COST_PER_1K_TOKENS));
        // ST4 template is normally loaded by Spring via @PostConstruct; this is a plain unit
        // test with no Spring context, so it must be invoked explicitly.
        orchestrator.loadTemplate();
    }

    private static AssessmentCommand.AssessmentCommandBuilder validCommandBuilder() {
        return AssessmentCommand.builder()
                .learningGoal("Evaluate whether students can implement iterative algorithms correctly")
                .topic("Array manipulation and loops")
                .level("introductory")
                .duration("60 minutes")
                .language("Java");
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
    void shouldReturnFullyPopulatedOutcomeWhenCommandIsValid() {
        // given
        AssessmentCommand command = validCommandBuilder().build();
        AssessmentResult result = completeResult();
        AssessmentGenerationResponse response = AssessmentGenerationResponse.builder()
                .result(result)
                .rawResponseText("{\"title\":\"Loop exercise\"}")
                .modelName("gemini-2.0-flash")
                .estimatedInputTokens(120)
                .estimatedOutputTokens(80)
                .build();
        when(assessmentGenerationPort.generate(anyString())).thenReturn(response);

        // when
        AssessmentExecutionOutcome outcome = orchestrator.generate(command);

        // then — 1. no nulo
        assertThat(outcome).isNotNull();
        // then — 2. atributos no nulos
        assertThat(outcome.result()).isNotNull();
        assertThat(outcome.log()).isNotNull();
        AgentExecutionLogPayload log = outcome.log();
        assertThat(log.agentExecutionId()).isNotNull();
        assertThat(log.agentName()).isNotNull();
        assertThat(log.model()).isNotNull();
        assertThat(log.promptVersion()).isNotNull();
        assertThat(log.inputHash()).isNotNull();
        assertThat(log.outputHash()).isNotNull();
        assertThat(log.estimatedInputTokens()).isNotNull();
        assertThat(log.estimatedOutputTokens()).isNotNull();
        assertThat(log.costEstimate()).isNotNull();
        assertThat(log.status()).isNotNull();
        assertThat(log.startedAt()).isNotNull();
        assertThat(log.finishedAt()).isNotNull();
        // then — 3. valores esperados
        assertThat(outcome.result()).isEqualTo(result);
        assertThat(log.agentName()).isEqualTo("assessment");
        assertThat(log.model()).isEqualTo("gemini-2.0-flash");
        assertThat(log.promptVersion()).isEqualTo("// assessment-generation.v1");
        assertThat(log.estimatedInputTokens()).isEqualTo(120);
        assertThat(log.estimatedOutputTokens()).isEqualTo(80);
        assertThat(log.status()).isEqualTo("COMPLETED");
        assertThat(log.errorCode()).isNull();
    }

    @Test
    void shouldRenderDifferentPromptWhenRegeneratingWithAdjustmentNotes() {
        // given
        AssessmentCommand initialCommand = validCommandBuilder().build();
        AssessmentCommand regenerationCommand = validCommandBuilder()
                .adjustmentNotes("Make it shorter, students only have 40 minutes now.")
                .previousDraftId("draft-123")
                .previousDraft("Title: Loop exercise. Objectives: use for loops.")
                .build();
        AssessmentGenerationResponse response = AssessmentGenerationResponse.builder()
                .result(completeResult())
                .rawResponseText("{\"title\":\"Loop exercise\"}")
                .modelName("gemini-2.0-flash")
                .estimatedInputTokens(100)
                .estimatedOutputTokens(50)
                .build();
        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        when(assessmentGenerationPort.generate(promptCaptor.capture())).thenReturn(response);

        // when
        orchestrator.generate(initialCommand);
        String initialPrompt = promptCaptor.getValue();
        orchestrator.generate(regenerationCommand);
        String regenerationPrompt = promptCaptor.getValue();

        // then
        assertThat(initialPrompt).isNotNull();
        assertThat(regenerationPrompt).isNotNull();
        assertThat(regenerationPrompt).isNotEqualTo(initialPrompt);
        assertThat(regenerationPrompt).contains("Title: Loop exercise. Objectives: use for loops.");
        assertThat(regenerationPrompt).contains("Make it shorter, students only have 40 minutes now.");
        assertThat(initialPrompt).doesNotContain("Make it shorter, students only have 40 minutes now.");
    }

    @Test
    void shouldThrowMalformedOutputWhenResultIsMissingRequiredField() {
        // given
        AssessmentCommand command = validCommandBuilder().build();
        AssessmentResult incompleteResult = AssessmentResult.builder()
                .title("Loop exercise")
                .context("")
                .instructions("Implement the requested program")
                .objectives(List.of("Use for loops"))
                .deliverables(List.of("Source code"))
                .constraints(List.of("No external libraries"))
                .build();
        AssessmentGenerationResponse response = AssessmentGenerationResponse.builder()
                .result(incompleteResult)
                .rawResponseText("{\"title\":\"Loop exercise\",\"context\":\"\"}")
                .modelName("gemini-2.0-flash")
                .estimatedInputTokens(100)
                .estimatedOutputTokens(50)
                .build();
        when(assessmentGenerationPort.generate(anyString())).thenReturn(response);

        // when / then
        assertThatThrownBy(() -> orchestrator.generate(command))
                .isInstanceOf(AssessmentAgentException.class)
                .satisfies(ex -> {
                    AssessmentAgentException agentException = (AssessmentAgentException) ex;
                    assertThat(agentException.reason()).isEqualTo(Reason.MALFORMED_OUTPUT);
                    assertThat(agentException.log()).isNotNull();
                    AgentExecutionLogPayload log = agentException.log();
                    // then — atributos no nulos (el port sí fue llamado antes de fallar la validación de salida)
                    assertThat(log.agentExecutionId()).isNotNull();
                    assertThat(log.agentName()).isNotNull();
                    assertThat(log.model()).isNotNull();
                    assertThat(log.promptVersion()).isNotNull();
                    assertThat(log.inputHash()).isNotNull();
                    assertThat(log.estimatedInputTokens()).isNotNull();
                    assertThat(log.estimatedOutputTokens()).isNotNull();
                    assertThat(log.costEstimate()).isNotNull();
                    assertThat(log.status()).isNotNull();
                    assertThat(log.errorCode()).isNotNull();
                    assertThat(log.startedAt()).isNotNull();
                    assertThat(log.finishedAt()).isNotNull();
                    // then — valores esperados
                    assertThat(log.agentName()).isEqualTo("assessment");
                    assertThat(log.model()).isEqualTo("gemini-2.0-flash");
                    assertThat(log.promptVersion()).isEqualTo("// assessment-generation.v1");
                    assertThat(log.estimatedInputTokens()).isEqualTo(100);
                    assertThat(log.estimatedOutputTokens()).isEqualTo(50);
                    assertThat(log.status()).isEqualTo("FAILED");
                    assertThat(log.errorCode()).isEqualTo("MALFORMED_OUTPUT");
                    // then — nunca se llegó a hashear una salida, porque la validación falló antes
                    assertThat(log.outputHash()).isNull();
                });
    }

    @Test
    void shouldThrowInvalidCommandWhenRequiredFieldIsBlank() {
        // given
        AssessmentCommand command = AssessmentCommand.builder()
                .learningGoal("")
                .topic("Array manipulation and loops")
                .level("introductory")
                .duration("60 minutes")
                .language("Java")
                .build();

        // when / then
        assertThatThrownBy(() -> orchestrator.generate(command))
                .isInstanceOf(AssessmentAgentException.class)
                .satisfies(ex -> assertInvalidCommandFailureLog((AssessmentAgentException) ex));
        verifyNoInteractions(assessmentGenerationPort);
    }

    @Test
    void shouldThrowInvalidCommandWhenRegenerationTripleIsIncomplete() {
        // given — adjustmentNotes present without previousDraftId/previousDraft
        AssessmentCommand command = validCommandBuilder()
                .adjustmentNotes("Make it shorter")
                .build();

        // when / then
        assertThatThrownBy(() -> orchestrator.generate(command))
                .isInstanceOf(AssessmentAgentException.class)
                .satisfies(ex -> assertInvalidCommandFailureLog((AssessmentAgentException) ex));
        verifyNoInteractions(assessmentGenerationPort);
    }

    @Test
    void shouldThrowInvalidCommandWhenProviderIsUnrecognized() {
        // given
        AssessmentCommand command = validCommandBuilder().provider("bogus").build();

        // when / then
        assertThatThrownBy(() -> orchestrator.generate(command))
                .isInstanceOf(AssessmentAgentException.class)
                .satisfies(ex -> assertInvalidCommandFailureLog((AssessmentAgentException) ex));
        verifyNoInteractions(assessmentGenerationPort);
    }

    @Test
    void shouldUseTheResolvedProviderOwnRateForCostEstimateNotAnotherProvidersRate() {
        // given — two providers, each with its own port and its own per-1K-token rate, wired
        // through the same Strategy-pattern selector this task introduces
        AssessmentGenerationPort geminiPort = org.mockito.Mockito.mock(AssessmentGenerationPort.class);
        AssessmentGenerationPort groqPort = org.mockito.Mockito.mock(AssessmentGenerationPort.class);
        AssessmentGenerationPortSelector twoProviderSelector = new AssessmentGenerationPortSelector(
                Map.of("gemini", geminiPort, "groq", groqPort), "groq");
        AssessmentAgentOrchestrator twoProviderOrchestrator = new AssessmentAgentOrchestrator(
                twoProviderSelector, Map.of("gemini", 0.000075, "groq", 0.0));
        twoProviderOrchestrator.loadTemplate();

        when(geminiPort.generate(anyString())).thenReturn(AssessmentGenerationResponse.builder()
                .result(completeResult())
                .rawResponseText("{\"title\":\"Loop exercise\"}")
                .modelName("gemini-2.0-flash")
                .estimatedInputTokens(1000)
                .estimatedOutputTokens(1000)
                .build());
        when(groqPort.generate(anyString())).thenReturn(AssessmentGenerationResponse.builder()
                .result(completeResult())
                .rawResponseText("{\"title\":\"Loop exercise\"}")
                .modelName("llama-3.3-70b-versatile")
                .estimatedInputTokens(1000)
                .estimatedOutputTokens(1000)
                .build());

        // when
        AssessmentExecutionOutcome geminiOutcome =
                twoProviderOrchestrator.generate(validCommandBuilder().provider("gemini").build());
        AssessmentExecutionOutcome groqOutcome =
                twoProviderOrchestrator.generate(validCommandBuilder().provider("groq").build());

        // then — identical token counts (2000 total), but each resolved provider's own rate
        // applies: 2000 / 1000.0 * rate
        assertThat(geminiOutcome.log().costEstimate()).isEqualTo(0.00015);
        assertThat(groqOutcome.log().costEstimate()).isEqualTo(0.0);
        assertThat(geminiOutcome.log().costEstimate()).isNotEqualTo(groqOutcome.log().costEstimate());
    }

    /**
     * Shared exhaustive assertions for the {@code INVALID_COMMAND} failure log — both
     * invalid-command tests reject before the port is ever called, so the log payload is
     * identically shaped in both: everything the port would have supplied stays {@code null}.
     */
    private static void assertInvalidCommandFailureLog(AssessmentAgentException agentException) {
        assertThat(agentException.reason()).isEqualTo(Reason.INVALID_COMMAND);
        assertThat(agentException.log()).isNotNull();
        AgentExecutionLogPayload log = agentException.log();
        // then — atributos no nulos, pese a que el port nunca se llamó
        assertThat(log.agentExecutionId()).isNotNull();
        assertThat(log.agentName()).isNotNull();
        assertThat(log.promptVersion()).isNotNull();
        assertThat(log.status()).isNotNull();
        assertThat(log.errorCode()).isNotNull();
        assertThat(log.startedAt()).isNotNull();
        assertThat(log.finishedAt()).isNotNull();
        // then — valores esperados
        assertThat(log.agentName()).isEqualTo("assessment");
        assertThat(log.promptVersion()).isEqualTo("// assessment-generation.v1");
        assertThat(log.status()).isEqualTo("FAILED");
        assertThat(log.errorCode()).isEqualTo("INVALID_COMMAND");
        // then — nada de esto se llegó a producir, porque la validación rechazó antes de llamar al port
        assertThat(log.model()).isNull();
        assertThat(log.inputHash()).isNull();
        assertThat(log.outputHash()).isNull();
        assertThat(log.estimatedInputTokens()).isNull();
        assertThat(log.estimatedOutputTokens()).isNull();
        assertThat(log.costEstimate()).isNull();
    }
}
