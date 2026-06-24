package cl.gradeops.ai.api.auth.application.orchestrator;

import cl.gradeops.ai.api.auth.application.command.IssuePasswordResetCodeCommand;
import cl.gradeops.ai.api.auth.application.command.SendPasswordResetEmailCommand;
import cl.gradeops.ai.api.auth.application.port.in.IssuePasswordResetCodeUseCase;
import cl.gradeops.ai.api.auth.application.port.in.SendPasswordResetEmailUseCase;
import cl.gradeops.ai.api.auth.application.port.out.EmailNotificationPort;
import cl.gradeops.ai.api.auth.application.result.IssuePasswordResetCodeResult;
import cl.gradeops.ai.api.domain.teacher.TeacherEntity;
import cl.gradeops.ai.api.domain.teacher.TeacherRepository;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class SendPasswordResetEmailOrchestrator implements SendPasswordResetEmailUseCase {

    private final TeacherRepository teacherRepository;
    private final IssuePasswordResetCodeUseCase issuePasswordResetCodeUseCase;
    private final EmailNotificationPort emailNotificationPort;
    private final int ttlMinutes;

    @Override
    public void execute(SendPasswordResetEmailCommand command) {
        Optional<TeacherEntity> maybeTeacher = teacherRepository.findByEmail(command.email());
        if (maybeTeacher.isEmpty()) return;

        TeacherEntity teacher = maybeTeacher.get();
        if (!"EMAIL_PASSWORD".equals(teacher.getProvider())) return;

        IssuePasswordResetCodeResult result = issuePasswordResetCodeUseCase.execute(
                IssuePasswordResetCodeCommand.builder()
                        .teacherUid(teacher.getFirebaseUid())
                        .ttlMinutes(ttlMinutes)
                        .build());

        emailNotificationPort.sendPasswordResetEmail(
                teacher.getEmail(), teacher.getFirstName(), result.rawCode());
    }
}
