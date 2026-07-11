package cl.gradeops.ai.agents.assessment.infrastructure.adapter.in.web;

import cl.gradeops.ai.agents.assessment.application.command.AssessmentCommand;
import cl.gradeops.ai.agents.assessment.application.port.in.GenerateAssessmentDraftUseCase;
import cl.gradeops.ai.agents.assessment.application.result.AssessmentExecutionOutcome;
import cl.gradeops.ai.agents.assessment.infrastructure.adapter.in.web.response.AssessmentExecutionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The only endpoint {@code api/}'s {@code agentclient} calls. Depends on the use case port, not
 * on the orchestrator or the Gemini adapter directly — this class knows nothing about the
 * pipeline's internals.
 *
 * <p>Gated the same property-based way as {@code AssessmentConfig} (see its Javadoc for why
 * {@code @ConditionalOnBean} would not work reliably here) — without this, the {@code test}
 * Spring profile would fail {@code GradeOpsAgentsApplicationTest#contextLoads} because {@code
 * GenerateAssessmentDraftUseCase} never gets created there.
 */
@RestController
@RequestMapping("/internal/agents/assessment")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.agents.gemini", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AssessmentController {

    private final GenerateAssessmentDraftUseCase generateAssessmentDraftUseCase;

    @PostMapping
    public ResponseEntity<AssessmentExecutionResponse> generate(@RequestBody AssessmentCommand command) {
        AssessmentExecutionOutcome outcome = generateAssessmentDraftUseCase.execute(command);
        return ResponseEntity.ok(AssessmentExecutionResponse.from(outcome));
    }
}
