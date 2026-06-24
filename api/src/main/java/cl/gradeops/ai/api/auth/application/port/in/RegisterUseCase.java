package cl.gradeops.ai.api.auth.application.port.in;

import cl.gradeops.ai.api.auth.application.command.RegisterCommand;
import cl.gradeops.ai.api.auth.application.result.RegisterResult;

public interface RegisterUseCase {
    RegisterResult execute(RegisterCommand command);
}
