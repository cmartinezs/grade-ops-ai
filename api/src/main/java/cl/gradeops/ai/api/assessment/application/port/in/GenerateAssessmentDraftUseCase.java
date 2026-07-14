package cl.gradeops.ai.api.assessment.application.port.in;

import cl.gradeops.ai.api.assessment.application.command.GenerateAssessmentDraftCommand;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;

public interface GenerateAssessmentDraftUseCase {
    GenerateAssessmentDraftResult execute(GenerateAssessmentDraftCommand command);
}
