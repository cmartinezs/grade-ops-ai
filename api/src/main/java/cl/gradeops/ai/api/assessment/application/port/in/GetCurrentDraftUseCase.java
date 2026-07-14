package cl.gradeops.ai.api.assessment.application.port.in;

import cl.gradeops.ai.api.assessment.application.command.GetCurrentDraftCommand;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;

public interface GetCurrentDraftUseCase {
    GenerateAssessmentDraftResult execute(GetCurrentDraftCommand command);
}
