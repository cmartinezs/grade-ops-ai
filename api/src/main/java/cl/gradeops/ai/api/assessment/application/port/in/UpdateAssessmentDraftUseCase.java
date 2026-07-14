package cl.gradeops.ai.api.assessment.application.port.in;

import cl.gradeops.ai.api.assessment.application.command.UpdateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;

public interface UpdateAssessmentDraftUseCase {
    GenerateAssessmentDraftResult execute(UpdateAssessmentDraftCommand command);
}
