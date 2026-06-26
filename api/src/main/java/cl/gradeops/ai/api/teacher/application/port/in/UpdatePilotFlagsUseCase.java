package cl.gradeops.ai.api.teacher.application.port.in;

import cl.gradeops.ai.api.teacher.application.command.UpdatePilotFlagsCommand;
import cl.gradeops.ai.api.teacher.application.result.UpdatePilotFlagsResult;

public interface UpdatePilotFlagsUseCase {
    UpdatePilotFlagsResult execute(UpdatePilotFlagsCommand command);
}
