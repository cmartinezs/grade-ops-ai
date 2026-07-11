package cl.gradeops.ai.agents.assessment.application.usecase;

import cl.gradeops.ai.agents.assessment.application.command.AssessmentCommand;
import cl.gradeops.ai.agents.assessment.application.orchestrator.AssessmentAgentOrchestrator;
import cl.gradeops.ai.agents.assessment.application.port.in.GenerateAssessmentDraftUseCase;
import cl.gradeops.ai.agents.assessment.application.result.AssessmentExecutionOutcome;
import lombok.RequiredArgsConstructor;

// NO @Service — registered as @Bean in AssessmentConfig
@RequiredArgsConstructor
public class GenerateAssessmentDraftHandler implements GenerateAssessmentDraftUseCase {

    private final AssessmentAgentOrchestrator orchestrator;

    @Override
    public AssessmentExecutionOutcome execute(AssessmentCommand command) {
        return orchestrator.generate(command);
    }
}
