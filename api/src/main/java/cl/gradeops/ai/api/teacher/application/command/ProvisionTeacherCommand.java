package cl.gradeops.ai.api.teacher.application.command;

import lombok.Builder;

import java.util.Objects;

@Builder
public record ProvisionTeacherCommand(String firstName, String lastName, String email) {
    public ProvisionTeacherCommand {
        Objects.requireNonNull(firstName, "firstName must not be null");
        Objects.requireNonNull(lastName,  "lastName must not be null");
        Objects.requireNonNull(email,     "email must not be null");
    }
}
