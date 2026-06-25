package cl.gradeops.ai.api.auth.application.command;

import lombok.Builder;
import java.util.Objects;

@Builder
public record SendPasswordResetEmailCommand(String email) {
    public SendPasswordResetEmailCommand {
        Objects.requireNonNull(email, "email must not be null");
    }
}
