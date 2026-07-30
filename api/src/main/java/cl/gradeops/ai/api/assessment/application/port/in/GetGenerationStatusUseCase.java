package cl.gradeops.ai.api.assessment.application.port.in;

import cl.gradeops.ai.api.assessment.application.command.GetGenerationStatusCommand;
import cl.gradeops.ai.api.assessment.application.result.GetGenerationStatusResult;

public interface GetGenerationStatusUseCase {
    GetGenerationStatusResult execute(GetGenerationStatusCommand command);
}
