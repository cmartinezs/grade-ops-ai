package cl.gradeops.ai.api.auth.application.command;

import cl.gradeops.ai.api.shared.application.exception.InvalidCommandException;
import lombok.Builder;

@Builder
public record RegisterCommand(String idToken, String firstName, String lastName) {
    public RegisterCommand {
        if (idToken == null) throw new InvalidCommandException("idToken");
    }
}
