package cl.gradeops.ai.api.auth.application.command;

import cl.gradeops.ai.api.shared.application.exception.InvalidCommandException;
import lombok.Builder;

@Builder
public record ResetPasswordCommand(String rawCode, String email,
                                    String password, String passwordRepeat) {
    public ResetPasswordCommand {
        if (rawCode == null)        throw new InvalidCommandException("rawCode");
        if (email == null)          throw new InvalidCommandException("email");
        if (password == null)       throw new InvalidCommandException("password");
        if (passwordRepeat == null) throw new InvalidCommandException("passwordRepeat");
    }
}
