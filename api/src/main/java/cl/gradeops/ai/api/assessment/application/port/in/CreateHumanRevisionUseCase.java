package cl.gradeops.ai.api.assessment.application.port.in;

import cl.gradeops.ai.api.assessment.application.command.CreateHumanRevisionCommand;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;

public interface CreateHumanRevisionUseCase {
    GenerateAssessmentDraftResult execute(CreateHumanRevisionCommand command);
}
