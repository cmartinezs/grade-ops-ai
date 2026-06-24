package cl.gradeops.ai.api.auth.application.port.in;

import cl.gradeops.ai.api.auth.application.command.SendPasswordResetEmailCommand;

public interface SendPasswordResetEmailUseCase {
    void execute(SendPasswordResetEmailCommand command);
}
