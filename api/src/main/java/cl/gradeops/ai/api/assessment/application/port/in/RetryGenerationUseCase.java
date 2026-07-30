package cl.gradeops.ai.api.assessment.application.port.in;

import cl.gradeops.ai.api.assessment.application.command.RetryGenerationCommand;
import cl.gradeops.ai.api.assessment.application.result.RetryGenerationResult;

public interface RetryGenerationUseCase {
    RetryGenerationResult execute(RetryGenerationCommand command);
}
