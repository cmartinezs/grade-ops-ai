package cl.gradeops.ai.api.assessment.application.port.in;

import cl.gradeops.ai.api.assessment.application.command.RegenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;

public interface RegenerateAssessmentDraftUseCase {
    GenerateAssessmentDraftResult execute(RegenerateAssessmentDraftCommand command);
}
