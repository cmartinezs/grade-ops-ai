package cl.gradeops.ai.api.auth.application.command;

import lombok.Builder;
import java.util.Objects;

@Builder
public record ResetPasswordCommand(String rawCode, String email,
                                    String password, String passwordRepeat) {
    public ResetPasswordCommand {
        Objects.requireNonNull(rawCode, "rawCode must not be null");
        Objects.requireNonNull(email, "email must not be null");
        Objects.requireNonNull(password, "password must not be null");
        Objects.requireNonNull(passwordRepeat, "passwordRepeat must not be null");
    }
}
