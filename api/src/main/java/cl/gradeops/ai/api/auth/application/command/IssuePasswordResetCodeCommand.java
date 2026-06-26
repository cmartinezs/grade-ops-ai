package cl.gradeops.ai.api.auth.application.command;

import cl.gradeops.ai.api.auth.domain.model.SignInProvider;
import cl.gradeops.ai.api.shared.application.exception.InvalidCommandException;
import lombok.Builder;

@Builder
public record IssuePasswordResetCodeCommand(String teacherUid, int ttlMinutes, SignInProvider provider) {
    public IssuePasswordResetCodeCommand {
        if (teacherUid == null) throw new InvalidCommandException("teacherUid");
        if (provider == null)   throw new InvalidCommandException("provider");
    }
}
