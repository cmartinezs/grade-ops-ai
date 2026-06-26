package cl.gradeops.ai.api.auth.application.orchestrator;

import cl.gradeops.ai.api.auth.application.command.IssuePasswordResetCodeCommand;
import cl.gradeops.ai.api.auth.application.command.SendPasswordResetEmailCommand;
import cl.gradeops.ai.api.auth.application.port.in.IssuePasswordResetCodeUseCase;
import cl.gradeops.ai.api.auth.application.port.in.SendPasswordResetEmailUseCase;
import cl.gradeops.ai.api.auth.application.port.out.EmailNotificationPort;
import cl.gradeops.ai.api.auth.application.result.IssuePasswordResetCodeResult;
import cl.gradeops.ai.api.auth.domain.model.SignInProvider;
import cl.gradeops.ai.api.teacher.application.port.out.TeacherRepositoryPort;
import cl.gradeops.ai.api.teacher.domain.model.AuthProvider;
import cl.gradeops.ai.api.teacher.domain.model.Teacher;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class SendPasswordResetEmailOrchestrator implements SendPasswordResetEmailUseCase {

    private final TeacherRepositoryPort teacherRepository;
    private final IssuePasswordResetCodeUseCase issuePasswordResetCodeUseCase;
    private final EmailNotificationPort emailNotificationPort;
    private final int ttlMinutes;

    @Override
    public void execute(SendPasswordResetEmailCommand command) {
        Optional<Teacher> maybeTeacher = teacherRepository.findByEmail(command.email());
        if (maybeTeacher.isEmpty()) return;

        Teacher teacher = maybeTeacher.get();
        if (teacher.getAuthProvider() != AuthProvider.EMAIL_PASSWORD) return;

        IssuePasswordResetCodeResult result = issuePasswordResetCodeUseCase.execute(
                IssuePasswordResetCodeCommand.builder()
                        .teacherUid(teacher.getFirebaseUid())
                        .ttlMinutes(ttlMinutes)
                        .provider(SignInProvider.EMAIL_PASSWORD)
                        .build());

        emailNotificationPort.sendPasswordResetEmail(
                teacher.getEmail(), teacher.getFirstName(), result.rawCode());
    }
}
