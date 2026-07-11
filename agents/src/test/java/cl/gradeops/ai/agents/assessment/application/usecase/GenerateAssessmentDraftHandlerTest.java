package cl.gradeops.ai.agents.assessment.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.gradeops.ai.agents.assessment.application.command.AssessmentCommand;
import cl.gradeops.ai.agents.assessment.application.orchestrator.AssessmentAgentOrchestrator;
import cl.gradeops.ai.agents.assessment.application.result.AgentExecutionLogPayload;
import cl.gradeops.ai.agents.assessment.application.result.AssessmentExecutionOutcome;
import cl.gradeops.ai.agents.assessment.application.result.AssessmentResult;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GenerateAssessmentDraftHandlerTest {

    @Mock
    private AssessmentAgentOrchestrator orchestrator;

    @InjectMocks
    private GenerateAssessmentDraftHandler handler;

    @Test
    void shouldDelegateToOrchestratorWhenExecutingCommand() {
        // given
        AssessmentCommand command = AssessmentCommand.builder()
                .learningGoal("Evaluate whether students can implement iterative algorithms correctly")
                .topic("Array manipulation and loops")
                .level("introductory")
                .duration("60 minutes")
                .language("Java")
                .build();
        AssessmentExecutionOutcome expectedOutcome = AssessmentExecutionOutcome.builder()
                .result(AssessmentResult.builder()
                        .title("Loop exercise")
                        .context("Practice iteration")
                        .instructions("Implement the requested program")
                        .objectives(List.of("Use for loops"))
                        .deliverables(List.of("Source code"))
                        .constraints(List.of("No external libraries"))
                        .build())
                .log(AgentExecutionLogPayload.builder()
                        .agentExecutionId(UUID.randomUUID())
                        .agentName("assessment")
                        .model("gemini-2.0-flash")
                        .promptVersion("// assessment-generation.v1")
                        .status("COMPLETED")
                        .startedAt(Instant.now())
                        .finishedAt(Instant.now())
                        .build())
                .build();
        when(orchestrator.generate(command)).thenReturn(expectedOutcome);

        // when
        AssessmentExecutionOutcome result = handler.execute(command);

        // then — 1. no nulo
        assertThat(result).isNotNull();
        // then — 2/3. es exactamente lo que retornó el orquestador, sin transformación
        assertThat(result).isSameAs(expectedOutcome);
        // then — 4. la delegación efectivamente ocurrió con el mismo command
        verify(orchestrator).generate(command);
    }
}
