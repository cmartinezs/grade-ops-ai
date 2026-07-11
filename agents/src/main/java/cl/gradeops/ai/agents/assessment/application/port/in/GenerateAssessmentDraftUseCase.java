package cl.gradeops.ai.agents.assessment.application.port.in;

import cl.gradeops.ai.agents.assessment.application.command.AssessmentCommand;
import cl.gradeops.ai.agents.assessment.application.result.AssessmentExecutionOutcome;

/** The contract {@code AssessmentController} (task-04) depends on. */
public interface GenerateAssessmentDraftUseCase {

    AssessmentExecutionOutcome execute(AssessmentCommand command);
}
