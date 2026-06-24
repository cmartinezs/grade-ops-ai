package cl.gradeops.ai.api.auth.application.command;

import lombok.Builder;
import java.util.Objects;

@Builder
public record IssuePasswordResetCodeCommand(String teacherUid, int ttlMinutes) {
    public IssuePasswordResetCodeCommand {
        Objects.requireNonNull(teacherUid, "teacherUid must not be null");
    }
}
