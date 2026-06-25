package cl.gradeops.ai.api.auth.application.command;

import cl.gradeops.ai.api.auth.domain.model.SignInProvider;
import lombok.Builder;
import java.util.Objects;

@Builder
public record IssuePasswordResetCodeCommand(String teacherUid, int ttlMinutes, SignInProvider provider) {
    public IssuePasswordResetCodeCommand {
        Objects.requireNonNull(teacherUid, "teacherUid must not be null");
        Objects.requireNonNull(provider, "provider must not be null");
    }
}
