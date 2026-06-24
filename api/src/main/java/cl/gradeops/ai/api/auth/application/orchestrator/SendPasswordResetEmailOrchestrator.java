package cl.gradeops.ai.api.auth.application.orchestrator;

import cl.gradeops.ai.api.auth.application.command.IssuePasswordResetCodeCommand;
import cl.gradeops.ai.api.auth.application.command.SendPasswordResetEmailCommand;
import cl.gradeops.ai.api.auth.application.port.in.IssuePasswordResetCodeUseCase;
import cl.gradeops.ai.api.auth.application.port.in.SendPasswordResetEmailUseCase;
import cl.gradeops.ai.api.auth.application.port.out.EmailNotificationPort;
import cl.gradeops.ai.api.auth.application.port.out.TeacherRepositoryPort;
import cl.gradeops.ai.api.auth.application.result.IssuePasswordResetCodeResult;
import cl.gradeops.ai.api.auth.domain.model.SignInProvider;
import cl.gradeops.ai.api.domain.teacher.TeacherEntity;
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
        Optional<TeacherEntity> maybeTeacher = teacherRepository.findByEmail(command.email());
        if (maybeTeacher.isEmpty()) return;

        TeacherEntity teacher = maybeTeacher.get();
        SignInProvider provider;
        try {
            provider = SignInProvider.valueOf(teacher.getProvider());
        } catch (IllegalArgumentException e) {
            return;
        }
        if (provider != SignInProvider.EMAIL_PASSWORD) return;

        IssuePasswordResetCodeResult result = issuePasswordResetCodeUseCase.execute(
                IssuePasswordResetCodeCommand.builder()
                        .teacherUid(teacher.getFirebaseUid())
                        .ttlMinutes(ttlMinutes)
                        .provider(provider)
                        .build());

        emailNotificationPort.sendPasswordResetEmail(
                teacher.getEmail(), teacher.getFirstName(), result.rawCode());
    }
}
