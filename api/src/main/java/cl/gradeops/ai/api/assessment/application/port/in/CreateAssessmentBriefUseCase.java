package cl.gradeops.ai.api.assessment.application.port.in;

import cl.gradeops.ai.api.assessment.application.command.CreateAssessmentBriefCommand;
import cl.gradeops.ai.api.assessment.application.result.CreateAssessmentBriefResult;

public interface CreateAssessmentBriefUseCase {
    CreateAssessmentBriefResult execute(CreateAssessmentBriefCommand command);
}
