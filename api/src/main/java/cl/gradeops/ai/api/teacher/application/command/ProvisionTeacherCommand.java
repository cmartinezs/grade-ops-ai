package cl.gradeops.ai.api.teacher.application.command;

import cl.gradeops.ai.api.shared.application.exception.InvalidCommandException;
import lombok.Builder;

@Builder
public record ProvisionTeacherCommand(String firstName, String lastName, String email) {
    public ProvisionTeacherCommand {
        if (firstName == null) throw new InvalidCommandException("firstName");
        if (lastName == null)  throw new InvalidCommandException("lastName");
        if (email == null)     throw new InvalidCommandException("email");
    }
}
