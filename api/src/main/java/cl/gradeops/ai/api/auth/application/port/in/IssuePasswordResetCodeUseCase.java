package cl.gradeops.ai.api.auth.application.port.in;

import cl.gradeops.ai.api.auth.application.command.IssuePasswordResetCodeCommand;
import cl.gradeops.ai.api.auth.application.result.IssuePasswordResetCodeResult;

public interface IssuePasswordResetCodeUseCase {
    IssuePasswordResetCodeResult execute(IssuePasswordResetCodeCommand command);
}
