package cl.gradeops.ai.api.teacher.application.port.in;

import cl.gradeops.ai.api.teacher.application.command.ProvisionTeacherCommand;
import cl.gradeops.ai.api.teacher.application.result.ProvisionTeacherResult;

public interface ProvisionTeacherUseCase {
    ProvisionTeacherResult execute(ProvisionTeacherCommand command);
}
