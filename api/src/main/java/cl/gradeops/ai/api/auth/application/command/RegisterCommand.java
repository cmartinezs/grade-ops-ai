package cl.gradeops.ai.api.auth.application.command;

import lombok.Builder;
import java.util.Objects;

@Builder
public record RegisterCommand(String idToken, String firstName, String lastName) {
    public RegisterCommand {
        Objects.requireNonNull(idToken, "idToken must not be null");
    }
}
