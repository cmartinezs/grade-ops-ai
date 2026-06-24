package cl.gradeops.ai.api.auth.application.port.in;

import cl.gradeops.ai.api.auth.application.command.ResetPasswordCommand;

public interface ResetPasswordUseCase {
    void execute(ResetPasswordCommand command);
}
