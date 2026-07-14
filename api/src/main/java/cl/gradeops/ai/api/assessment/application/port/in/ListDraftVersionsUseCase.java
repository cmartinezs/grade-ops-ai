package cl.gradeops.ai.api.assessment.application.port.in;

import cl.gradeops.ai.api.assessment.application.command.ListDraftVersionsCommand;
import cl.gradeops.ai.api.assessment.application.result.GenerateAssessmentDraftResult;

import java.util.List;

public interface ListDraftVersionsUseCase {
    List<GenerateAssessmentDraftResult> execute(ListDraftVersionsCommand command);
}
