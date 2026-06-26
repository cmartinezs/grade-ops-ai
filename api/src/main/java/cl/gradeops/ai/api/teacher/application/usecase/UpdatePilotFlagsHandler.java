package cl.gradeops.ai.api.teacher.application.usecase;

import cl.gradeops.ai.api.teacher.application.command.UpdatePilotFlagsCommand;
import cl.gradeops.ai.api.teacher.application.port.in.UpdatePilotFlagsUseCase;
import cl.gradeops.ai.api.teacher.application.port.out.TeacherRepositoryPort;
import cl.gradeops.ai.api.teacher.application.result.UpdatePilotFlagsResult;
import cl.gradeops.ai.api.teacher.domain.exception.TeacherNotFoundException;
import cl.gradeops.ai.api.teacher.domain.model.Teacher;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

// NO @Service — declared as @Bean in TeacherConfig
@RequiredArgsConstructor
public class UpdatePilotFlagsHandler implements UpdatePilotFlagsUseCase {

    private final TeacherRepositoryPort teacherRepository;

    @Override
    @Transactional
    public UpdatePilotFlagsResult execute(UpdatePilotFlagsCommand command) {
        Teacher teacher = teacherRepository.findById(command.uid())
            .orElseThrow(() -> new TeacherNotFoundException(command.uid()));

        teacher.updatePilotFlags(
            command.planType(), command.relatedParty(),
            command.offerDetails(), command.evidenceLink(), command.setBy()
        );
        teacherRepository.save(teacher);

        return UpdatePilotFlagsResult.builder()
            .firebaseUid(teacher.getFirebaseUid())
            .planType(teacher.getPlanType())
            .relatedParty(teacher.isRelatedParty())
            .flagSetAt(teacher.getFlagSetAt() != null ? teacher.getFlagSetAt().toString() : null)
            .build();
    }
}
